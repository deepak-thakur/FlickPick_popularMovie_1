package Deepak_Thakur.FlickPick.com.FlickPick;

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

import Deepak_Thakur.FlickPick.com.FlickPick.adapter.MovieAdapter;
import Deepak_Thakur.FlickPick.com.FlickPick.model.Movie;
import Deepak_Thakur.FlickPick.com.FlickPick.utils.MovieJsonUtils;
import Deepak_Thakur.FlickPick.com.FlickPick.utils.MovieUrlUtils;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String CALLBACK_QUERY = "callbackQuery";
    private static final String CALLBACK_NAMESORT= "callbackNamesort";
    private String query_Movie = "popular";
    private String nameSort = "Popular Movies";
    private Movie[] mMovie = null;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private TextView tv_error;
    private Button button_retry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rv_main);
        button_retry = findViewById(R.id.btn_retry);

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
                query_Movie = savedInstanceState.getString(CALLBACK_QUERY);
                nameSort = savedInstanceState.getString(CALLBACK_NAMESORT);
                setTitle(nameSort);
                new  MovieFechtTask().execute(query_Movie);
                return;
            }
        }
        new MovieFechtTask().execute(query_Movie);

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
                query_Movie = "popular";
                new MovieFechtTask().execute(query_Movie);
                nameSort = "Popular Movies";
                setTitle(nameSort);
                break;
            case R.id.top_rated:
                query_Movie = "top_rated";
                new MovieFechtTask().execute(query_Movie);
                nameSort = "Top Rated Movies";
                setTitle(nameSort);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void errorNetworkApi() {
        progressBar.setVisibility(View.INVISIBLE);
        tv_error.setVisibility(View.VISIBLE);
        button_retry.setVisibility(View.VISIBLE);
    }

    public void clickRetry(View view) {
        if (!isOnline()) {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            view.startAnimation(shake);
            return;
        }
        query_Movie = "popular";
        button_retry.setVisibility(View.INVISIBLE);
        tv_error.setVisibility(View.INVISIBLE);
        new MovieFechtTask().execute(query_Movie);
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
        intent.putExtra("backdropimage",mMovie[position].getBackdropPoster());
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String queryMovieSaved = query_Movie;
        String nameSortSaved = nameSort;
        outState.putString(CALLBACK_QUERY, queryMovieSaved);
        outState.putString(CALLBACK_NAMESORT, nameSortSaved);

    }

    @SuppressLint("StaticFieldLeak")
    private class MovieFechtTask extends AsyncTask<String, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mRecyclerView.setVisibility(View.VISIBLE);
            //mRecyclerView.setVisibility(View.INVISIBLE);
            //progressBar.setVisibility(View.VISIBLE);
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
                button_retry.setVisibility(View.INVISIBLE);
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


