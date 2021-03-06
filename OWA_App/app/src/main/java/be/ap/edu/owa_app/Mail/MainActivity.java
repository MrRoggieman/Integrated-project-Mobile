package be.ap.edu.owa_app.Mail;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.identity.client.MsalClientException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;

import be.ap.edu.owa_app.Calendar.CalendarActivity;
import be.ap.edu.owa_app.Contacts.ContactsActivity;
import be.ap.edu.owa_app.LoginActivity;
import be.ap.edu.owa_app.R;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeContainer;

    final static String CLIENT_ID = "0f1fbbeb-1161-4034-9875-70c8099230d7";
    final static String SCOPES[] = {"https://graph.microsoft.com/Mail.Read",
            "https://graph.microsoft.com/Mail.ReadWrite",
            "https://graph.microsoft.com/Mail.Send",
            "https://graph.microsoft.com/Calendars.Read" };
    String MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me/mailfolders/inbox/messages?$top=50";

    private ArrayList<MailList> maillist = new ArrayList<>();
    private ListAdapter listadapter;

    private ArrayList<MailFolder> mailFolders = new ArrayList<>();

    /* UI & Debugging Variables */
    private static final String TAG = MainActivity.class.getSimpleName();


    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;



    ListView listView;
    private String token;
    private int positie;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Inbox");

        token = this.getIntent().getExtras().getString("token");
        maillist = new ArrayList<>();


        Log.d("messagetoken", token);


        getMailFolders(token, "https://graph.microsoft.com/v1.0/me/mailFolders/");
        Log.d("mailContext", "SizeOncreate" + mailFolders.size());
        token = this.getIntent().getExtras().getString("token");
        positie = this.getIntent().getExtras().getInt("position");
        String id = this.getIntent().getExtras().getString("id");

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setupDrawer();


        MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me/mailfolders/inbox/messages";




        callGraphAPI(token, MSGRAPH_URL);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.action_home);
            if (bottomNavigationView != null) {
                // Set action to perform when any menu-item is selected.
                bottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            selectFragment(item);
                            return false;
                        }
                    });
        }



        listView = findViewById(R.id.mobile_list);

        listadapter = new ListAdapter(this, R.layout.activity_listviewmails, maillist);
        listView.setAdapter(listadapter);

        final Context context = this;
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // 1
                    MailList mail = maillist.get(position);

                    // 2
                    Intent intent = new Intent(context, OpenMailActivity.class);

                    // 3
                    intent.putExtra("id", mail.getId());
                    intent.putExtra("sender", mail.getSender());
                    intent.putExtra("onderwerp", mail.getSubject());
                    intent.putExtra("date", mail.getDate());
                    intent.putExtra("message", mail.getMessage());
                    intent.putExtra("token", token);
                    intent.putExtra("isRead", mail.isRead());
                    intent.putExtra("hasAttachments", mail.getHasAttachments());

                    // 4
                    startActivity(intent);
                }

            });
        }

    }

    private void addDrawerItems() {
        String[] osArray = new String[mailFolders.size()];

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //    Toast.makeText(MainActivity.this, "Time for an upgrade! " + osArray[position] + " " + position, Toast.LENGTH_SHORT).show();

                positie = position;
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("position", positie);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Mappen");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    protected void selectFragment(MenuItem item) {

        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.action_home:
                // Action to perform when Home Menu item is selected.

                break;
            case R.id.action_calendar:
                // Action to perform when Bag Menu item is selected.
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra("token",token);
                startActivity(intent);
                break;
            case R.id.action_contacts:
                // Action to perform when Bag Menu item is selected.
                Intent intent2 = new Intent(MainActivity.this, ContactsActivity.class);
                intent2.putExtra("token",token);
                startActivity(intent2);
                break;
        }
    }




    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maillist, menu);

        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        //handle presses on the action bar items
        switch (item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_search:
                Intent in = new Intent(MainActivity.this, SearchScreenActivity.class);
                in.putExtra("token", token);
                startActivity(in);
                return true;

            case R.id.action_create_mail:
                Intent intent = new Intent(MainActivity.this, Sendmail.class);
                intent.putExtra("token", token);
                startActivity(intent);
                return true;


            case R.id.action_logout:
                onSignOutClicked();
                return true;


        }
        return super.onOptionsItemSelected(item);
    }
    private void callGraphAPI(final String token, String url) {
        Log.d(TAG, "Starting volley request to graph");

    /* Make sure we have a token to send to graph */
        if (token == null) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
            parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                updateGraphUI(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Log.d(TAG, "Adding HTTP GET to Queue, Request: " + request.toString());

        request.setRetryPolicy(new DefaultRetryPolicy(
            3000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private void getMailFolders(final String token, String url) {
        Log.d(TAG, "Starting volley request to graph");

    /* Make sure we have a token to send to graph */
        if (token == null) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                try {
                    JSONArray subject = response.getJSONArray("value");
                    for (int i = 0; i < subject.length(); i++) {
                        String id = subject.getJSONObject(i).getString("id");
                        String name = subject.getJSONObject(i).getString("displayName");
                        int totalItemCount = subject.getJSONObject(i).getInt("totalItemCount");
                        if(totalItemCount > 0) {
                            mailFolders.add(new MailFolder(id, name, totalItemCount));
                            Log.d("Mailfolder", name);
                        }
                    }


                    final String[] osName = new String[mailFolders.size()];
                    final String[] osID = new String[mailFolders.size()];
                    int i = 0;
                    for(MailFolder f : mailFolders){

                        osName[i] = f.getName();
                        osID[i] = f.getId();
                        i++;
                    }

                    mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, osName);
                    mDrawerList.setAdapter(mAdapter);

                    mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            listadapter.clear();
                            listadapter.notifyDataSetChanged();

                            MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me/mailfolders/" + osID[position] + "/messages?$top=50";
                            callGraphAPI(token, MSGRAPH_URL);
                            positie = position;
                            getSupportActionBar().setTitle(osName[position]);
                            mDrawerLayout.closeDrawers();
                            mDrawerToggle.setDrawerIndicatorEnabled(true);
                            setupDrawer();
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                        }
                    });

                    mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

                        /** Called when a drawer has settled in a completely open state. */
                        public void onDrawerOpened(View drawerView) {
                            super.onDrawerOpened(drawerView);
                            getSupportActionBar().setTitle("Mappen");
                            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                        }

                        /** Called when a drawer has settled in a completely closed state. */
                        public void onDrawerClosed(View view) {
                            super.onDrawerClosed(view);
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setDisplayShowHomeEnabled(true);
                            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                        }
                    };

                    mDrawerToggle.setDrawerIndicatorEnabled(true);
                    mDrawerLayout.setDrawerListener(mDrawerToggle);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Log.d(TAG, "Adding HTTP GET to Queue, Request: " + request.toString());

        request.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /* Sets the Graph response */
    private void updateGraphUI(JSONObject graphResponse) {
        try {


            JSONArray subject = graphResponse.getJSONArray("value");
            for (int i = 0; i < subject.length(); i++) {
                String sender;

                String id = (subject.getJSONObject(i).getString("id"));
                String subj = (subject.getJSONObject(i).get("subject")).toString();
                String bodypr = (subject.getJSONObject(i).getString("bodyPreview"));
                Boolean read = (subject.getJSONObject(i).getBoolean("isRead"));
                String message = (subject.getJSONObject(i).getJSONObject("body").getString("content"));
                String date = (subject.getJSONObject(i).getString("receivedDateTime"));
                Boolean hasAttachments = subject.getJSONObject(i).getBoolean("hasAttachments");
                if(subject.getJSONObject(i).has("sender")) {
                    sender = (subject.getJSONObject(i).getJSONObject("sender").getJSONObject("emailAddress").get("name")).toString();
                    maillist.add(new MailList(id, subj, sender, bodypr, read, message, date, hasAttachments));
                }else {
                    maillist.add(new MailList(id, subj, bodypr, read, message, date, hasAttachments));
                }
            }
            listView.requestLayout();
            listadapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSignOutClicked() {
        PublicClientApplication sampleApp;
        sampleApp = null;
        if (sampleApp == null) {
            sampleApp = new PublicClientApplication(
                    this.getApplicationContext(),
                    CLIENT_ID);
        }

    /* Attempt to get a user and remove their cookies from cache*/
        List<User> users = null;

        try {
            users = sampleApp.getUsers();

            if (users == null) {
            /* We have no users*/

            } else if (users.size() == 1) {
            /* We have 1 user */
            /* Remove from token cache*/
                sampleApp.remove(users.get(0));

            }
            else {
            /* We have multiple users*/
                for (int i = 0; i < users.size(); i++) {
                    sampleApp.remove(users.get(i));
                }
            }

            Toast.makeText(getBaseContext(), "Signed Out!", Toast.LENGTH_SHORT)
                    .show();

        } catch (MsalClientException e) {
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
        }


        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
