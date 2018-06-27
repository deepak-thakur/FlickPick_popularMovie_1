package Deepak_Thakur.FlickPick.com.FlickPick.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Deepak_Thakur.FlickPick.com.FlickPick.model.Movie;


public class MovieJsonUtils {


    private static final String mMOVIE_RESULTS = "results";
    private static final String mMOVIE_TITLE = "title";
    private static final String mMOVIE_POSTER = "poster_path";
    private static final String mMOVIE_PLOT = "overview";
    private static final String mMOVIE_RATING = "vote_average";
    private static final String mMOVIE_RELEASE_DATE = "release_date";
    private static final String mMovie_Backdrop="backdrop_path";




    public static Movie[] parseJsonMoview(String jsonMoviesData) throws JSONException {

        JSONObject jsonRoot = new JSONObject(jsonMoviesData);
        JSONArray jsonArrayResult = jsonRoot.getJSONArray(mMOVIE_RESULTS);
        Movie[] result = new Movie[jsonArrayResult.length()];
        for (int i = 0; i < jsonArrayResult.length(); i++) {
            Movie movie = new Movie();
            movie.setmTitle(jsonArrayResult.getJSONObject(i).optString(mMOVIE_TITLE));
            movie.setmMoviePoster(jsonArrayResult.getJSONObject(i).optString(mMOVIE_POSTER));
            movie.setmPlot(jsonArrayResult.getJSONObject(i).optString(mMOVIE_PLOT));
            movie.setmRating(jsonArrayResult.getJSONObject(i).optString(mMOVIE_RATING));
            movie.setmReleaseDate(jsonArrayResult.getJSONObject(i).optString(mMOVIE_RELEASE_DATE));
            movie.setBackdropPoster((jsonArrayResult.getJSONObject(i).optString(mMovie_Backdrop)));
            result[i] = movie;
        }
        return result;
    }

}
