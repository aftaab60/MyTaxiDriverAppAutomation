package com.mytaxi.android_demo.activities;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mytaxi.android_demo.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest_v2 {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    //default username and password
    private String userName = "crazydog335";
    private String password = "venture";
    CountingIdlingResource espressoTestIdlingResource = null;

    @Before
    public void setUp() throws Exception {
        //Set username and password variables with user defined values dynamically.
        //If user is not providing these values, use default values

        /*
        Logic is to read userName and password from runtime arguments
        that user can pass as a command line argument or in the parameters of CircleCI/Jenkins jobs
        */
        Bundle args = InstrumentationRegistry.getArguments();
        if(args.containsKey("userName")){
            userName = args.getString("userName");
        }
        if(args.containsKey("password")) {
            password = args.getString("password");
        }

        /*
        Synchronisation (dynamic wait) object.
        This will increase test stability by waiting dynamically for app-loading after login and search suggestions display on typing 'sa'
         */
        espressoTestIdlingResource = new CountingIdlingResource("Network-call");
    }

    @Test
    public void appTestCase(){
        loginTest();
        searchDriverAndCallTest();
        logoutTest();
    }

    @After
    public void tearDown() throws Exception {
        mActivityRule.finishActivity();
    }

    public void loginTest(){
        /*
        Login step should be executed only once since we are not terminating user login session upon exiting the app.
        Re-executing this test on same emulator will fail since user will be landed on home page instead of login page.

        2 options to handle this.
        1. Provide conditional login (Login only if user is not on home page upon opening the app)
        2. Add another step to logout at the end of test. This will ensure users will land on login page always when opens the app.
        */
        boolean userLoggedIn;
        try {
            onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
            userLoggedIn = false;
        }catch(Exception e){
            userLoggedIn = true;
            System.out.println("User '" + userName + "' is already logged in to the app");
        }

        if(!userLoggedIn) {
            //proceed with login
            onView(withId(R.id.edt_username)).perform(typeText(userName));
            onView(withId(R.id.edt_password)).perform(typeText(password));
            espressoTestIdlingResource.increment();
            onView(withId(R.id.btn_login)).perform(click());
            //Validate that user login is successful after clicking Login button by verifying an element in HOME page
            try{
                onView(withId(R.id.textSearch)).check(matches(isDisplayed()));
                System.out.println("Login successful");
            }
            catch(Exception e){
                fail("Login Failed!");
            }
            espressoTestIdlingResource.decrement();
        }
    }

    public void searchDriverAndCallTest(){
        String input = "sa";
        String secondResult = null;
        //clear search field and type input
        //check if search suggestions are displaying, retry max 3 times for suggestions and break loop when popped-up.
        int retryMax = 3;
        boolean suggestionsDisplayed = false;
        while(retryMax > 0 && suggestionsDisplayed==false){
            espressoTestIdlingResource.increment();
            onView(withId(R.id.textSearch)).perform(clearText()).perform(typeText(input), closeSoftKeyboard());
            try{
                //verify if autocomplete suggestions at position 2 is visible
                onData(withId(R.id.searchContainer)).atPosition(2).check(matches(isDisplayed()));
                //In case of selection by name, we should be knowing name to search for.
                //One way is to fetch list of all names from autocomplete as a <List> and look for second value
                //Second way is to provide name directly and select from autocomplete (Below code for option-2)

                //select autocomplete by value
                secondResult = "Sarah Scott";
                onView(withText(secondResult)).inRoot(RootMatchers.withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView())))).perform(click());
                break;
            }catch(Exception e){
                retryMax--;
            }
            espressoTestIdlingResource.decrement();
        }
        System.out.println("Driver '"+secondResult+"' selected");
        //wait for call button to display
        espressoTestIdlingResource.increment();
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
        espressoTestIdlingResource.decrement();
        //click on call button
        onView(withId(R.id.fab)).perform(click());
    }

    public void logoutTest(){
        //logout code will appear here
    }
}