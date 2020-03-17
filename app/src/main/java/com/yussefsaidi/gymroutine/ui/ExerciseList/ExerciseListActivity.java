package com.yussefsaidi.gymroutine.ui.ExerciseList;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.RequestManager;
import com.yussefsaidi.gymroutine.R;
import com.yussefsaidi.gymroutine.persistence.models.Exercise;
import com.yussefsaidi.gymroutine.util.VerticalSpacingItemDecorator;
import com.yussefsaidi.gymroutine.viewmodels.ViewModelProviderFactory;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class ExerciseListActivity extends DaggerAppCompatActivity implements View.OnClickListener {

    private static final int EDIT_MODE_DISABLED = 0;
    private static final int EDIT_MODE_ENABLED = 1;
    private static final String TAG = "ExerciseListActivity";

    @Inject
    ExerciseRecyclerAdapter adapter;

    @Inject
    ViewModelProviderFactory providerFactory;

    @Inject
    RequestManager requestManager;

    // ui


    // vars
    List<Exercise> exerciseList = new ArrayList<>();
    private ExerciseListViewModel viewModel;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_exercise_list);
        mRecyclerView = findViewById(R.id.recyclerView);
        initRecyclerView();
        viewModel = new ViewModelProvider(this, providerFactory).get(ExerciseListViewModel.class);
        subscribeObservers();
        findViewById(R.id.fab).setOnClickListener(this);

    }

    public void subscribeObservers() {
        viewModel.getAllExercises()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<List<Exercise>>() {
                    @Override
                    public void onNext(List<Exercise> exercises) {
                        exerciseList.clear();
                        exerciseList.addAll(exercises);
                        adapter.setExercises(exercises);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: CANT GET EXERCISES FROM DATABASE");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void initRecyclerView(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        VerticalSpacingItemDecorator verticalSpacingItemDecorator = new VerticalSpacingItemDecorator(3);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(verticalSpacingItemDecorator);
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(adapter);
    }

    private ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            deleteExercise(exerciseList.get(viewHolder.getAdapterPosition()));
        }
    };

    private void deleteExercise(Exercise exercise){
        viewModel.deleteExercise(exercise);
    }

    //On Click of the FAB
    @Override
    public void onClick(View view) {
        viewModel.insertExercises();
    }
}
