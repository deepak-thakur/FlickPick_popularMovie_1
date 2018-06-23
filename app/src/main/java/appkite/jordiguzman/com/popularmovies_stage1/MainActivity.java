package appkite.jordiguzman.com.popularmovies_stage1;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;

import appkite.jordiguzman.com.popularmovies_stage1.adapter.MovieAdapter;
import appkite.jordiguzman.com.popularmovies_stage1.model.Movie;
import appkite.jordiguzman.com.popularmovies_stage1.utils.MovieJsonUtils;
import appkite.jordiguzman.com.popularmovies_stage1.utils.MovieUrlUtils;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String CALLBACK_QUERY = "callbackQuery";
    private static final String CALLBACK_NAMESORT= "callbackNamesort";
    private String queryMovie = "popular";
    private String nameSort = "Popular Movies";
    private Movie[] mMovie = null;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private TextView tv_error;
    private Button btn_retry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rv_main);
        btn_retry = findViewById(R.id.btn_retry);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setHasFixedSize(true);

        progressBar = findViewById(R.id.pb_main);
        tv_error = findViewById(R.id.tv_error);

        setTitle(nameSort);
        if (!isOnline()) {
            errorNetworkApi();
            return;
        }

        if (savedInstanceState != null){
            if (savedInstanceState.containsKey(CALLBACK_QUERY) || savedInstanceState.containsKey(CALLBACK_NAMESORT)){
                queryMovie = savedInstanceState.getString(CALLBACK_QUERY);
                nameSort = savedInstanceState.getString(CALLBACK_NAMESORT);
                setTitle(nameSort);
                new  MovieFechtTask().execute(queryMovie);
                return;
            }
        }
        new MovieFechtTask().execute(queryMovie);

    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isOnline()) return false;
        if (MovieUrlUtils.API_KEY.equals("")) return false;
        int id = item.getItemId();
        switch (id) {
            case R.id.popularity:
                queryMovie = "popular";
                new MovieFechtTask().execute(queryMovie);
                nameSort = "Popular Movies";
                setTitle(nameSort);
                break;
            case R.id.top_rated:
                queryMovie = "top_rated";
                new MovieFechtTask().execute(queryMovie);
                nameSort = "Top Rated Movies";
                setTitle(nameSort);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void errorNetworkApi() {
        progressBar.setVisibility(View.INVISIBLE);
        tv_error.setVisibility(View.VISIBLE);
        btn_retry.setVisibility(View.VISIBLE);
    }

    public void clickRetry(View view) {
        if (!isOnline()) {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            view.startAnimation(shake);
            return;
        }
        queryMovie = "popular";
        btn_retry.setVisibility(View.INVISIBLE);
        tv_error.setVisibility(View.INVISIBLE);
        new MovieFechtTask().execute(queryMovie);
    }

    private void hideProgressAndTextview() {
        progressBar.setVisibility(View.INVISIBLE);
        tv_error.setVisibility(View.INVISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClickMovie(int position) {

        if (!isOnline()) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            errorNetworkApi();
            return;
        }

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("title", mMovie[position].getmTitle());
        intent.putExtra("poster", mMovie[position].getmMoviePoster());
        intent.putExtra("plot", mMovie[position].getmPlot());
        intent.putExtra("rating", mMovie[position].getmRating());
        intent.putExtra("releaseDate", mMovie[position].getmReleaseDate());
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String queryMovieSaved = queryMovie;
        String nameSortSaved = nameSort;
        outState.putString(CALLBACK_QUERY, queryMovieSaved);
        outState.putString(CALLBACK_NAMESORT, nameSortSaved);

    }

    @SuppressLint("StaticFieldLeak")
    private class MovieFechtTask extends AsyncTask<String, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mRecyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie[] doInBackground(String... strings) {
            if (!isOnline()) {
                errorNetworkApi();
                return null;
            }
            if (MovieUrlUtils.API_KEY.equals("")) {
                errorNetworkApi();
                tv_error.setText(R.string.missing_api_key);
                btn_retry.setVisibility(View.INVISIBLE);
                return null;
            }
            URL movieUrl = MovieUrlUtils.buildUrl(strings[0]);

            String movieResponse;
            try {
                movieResponse = MovieUrlUtils.getResponseFromHttp(movieUrl);
                mMovie = MovieJsonUtils.parseJsonMoview(movieResponse);
            } catch (Exception e) {

                e.printStackTrace();
            }
            return mMovie;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            new MovieFechtTask().cancel(true);
            if (movies != null) {
                mRecyclerView.setVisibility(View.VISIBLE);
                hideProgressAndTextview();
                mMovie = movies;
                MovieAdapter movieAdapter = new MovieAdapter(movies, MainActivity.this, MainActivity.this);
                mRecyclerView.setAdapter(movieAdapter);

            } else {
                Log.e(LOG_TAG, "Problems with adapter");
            }
        }

    }

}


