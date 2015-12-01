package info.androidhive.swiperefresh.activity;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import info.androidhive.swiperefresh.R;
import info.androidhive.swiperefresh.app.MyApplication;
import info.androidhive.swiperefresh.helper.Movie;
import info.androidhive.swiperefresh.helper.SwipeListAdapter;

public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener,SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {
    private SearchView mSearchView;

    private String TAG = MainActivity.class.getSimpleName();
    private String URL_TOP_250 = "http://api.androidhive.info/json/imdb_top_250.php?offset=";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private SwipeListAdapter adapter;
    private List<Movie> movieList;
    private int offSet = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        movieList = new ArrayList<>();
        adapter = new SwipeListAdapter(this, movieList);
        listView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        fetchMovies();
                                    }
                                }
        );

    }
    @Override
    public void onRefresh() {
        fetchMovies();
    }
    private void fetchMovies() {
        adapter = new SwipeListAdapter(this, movieList);
        listView.setAdapter(adapter);

        swipeRefreshLayout.setRefreshing(true);
        String url = URL_TOP_250 + offSet;
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject movieObj = response.getJSONObject(i);
                                    int rank = movieObj.getInt("rank");
                                    String title = movieObj.getString("title");
                                    Movie m = new Movie(rank, title);
                                    movieList.add(i, m);
                                    if (rank >= offSet)
                                        offSet = rank;
                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                }
                            }

                            adapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Server Error: " + error.getMessage());

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        MyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        setupSearchView();

        return true;
    }

    private void setupSearchView() {

        mSearchView.setIconifiedByDefault(true);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            // Try to use the "applications" global search provider
            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            mSearchView.setSearchableInfo(info);
        }

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query.toString());
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText.toString());
        return false;
    }

    void search(String data){
        if(data.equals("")){
            adapter = new SwipeListAdapter(this, movieList);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else{
            List<Movie> mlist = movieList;
            for(int i=0;i<movieList.size();i++){
                if(movieList.get(i).title.contains(data)){
                    Movie m = new Movie(i, movieList.get(i).title);
                    mlist.add(i, m);
                }
            }
            adapter = new SwipeListAdapter(this, mlist);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_search){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
