package com.opencbs.androidclient.modules;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencbs.androidclient.ApiRequestInterceptor;
import com.opencbs.androidclient.App;
import com.opencbs.androidclient.Settings;
import com.opencbs.androidclient.api.BranchApi;
import com.opencbs.androidclient.api.CityApi;
import com.opencbs.androidclient.api.CustomFieldApi;
import com.opencbs.androidclient.api.DistrictApi;
import com.opencbs.androidclient.api.EconomicActivityApi;
import com.opencbs.androidclient.api.PersonApi;
import com.opencbs.androidclient.api.RegionApi;
import com.opencbs.androidclient.repos.BranchRepo;
import com.opencbs.androidclient.repos.CityRepo;
import com.opencbs.androidclient.repos.ClientRepo;
import com.opencbs.androidclient.repos.CustomFieldRepo;
import com.opencbs.androidclient.repos.DistrictRepo;
import com.opencbs.androidclient.repos.EconomicActivityRepo;
import com.opencbs.androidclient.repos.PersonRepo;
import com.opencbs.androidclient.repos.RegionRepo;
import com.opencbs.androidclient.activities.BranchPickerActivity;
import com.opencbs.androidclient.fragments.ClientsFragment;
import com.opencbs.androidclient.fragments.DownloadFragment;
import com.opencbs.androidclient.activities.EconomicActivityPickerActivity;
import com.opencbs.androidclient.fragments.EndpointFragment;
import com.opencbs.androidclient.activities.LoginActivity;
import com.opencbs.androidclient.fragments.LoginFragment;
import com.opencbs.androidclient.activities.MainActivity;
import com.opencbs.androidclient.activities.PersonActivity;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

@Module(
        injects = {
                App.class,
                LoginActivity.class,
                MainActivity.class,
                ClientsFragment.class,
                LoginFragment.class,
                DownloadFragment.class,
                EndpointFragment.class,
                PersonActivity.class,
                EconomicActivityPickerActivity.class,
                BranchPickerActivity.class,

                BranchRepo.class,
                CityRepo.class,
                DistrictRepo.class,
                RegionRepo.class,
                EconomicActivityRepo.class,
                CustomFieldRepo.class,
                PersonRepo.class,
                ClientRepo.class,
        },
        library = true
)
public class AppModule {
    private App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return app;
    }

    @Provides
    @Singleton
    public EventBus provideBus() {
        return new EventBus();
    }

    @Provides
    @Singleton
    public JobManager provideJobManager() {
        Configuration configuration = new Configuration.Builder(app)
                .minConsumerCount(1)
                .maxConsumerCount(3)
                .loadFactor(3)
                .consumerKeepAlive(120)
                .build();
        return new JobManager(app, configuration);
    }

    @Provides
    public PersonApi providePersonApi() {
        return getRestAdapter().create(PersonApi.class);
    }

    @Provides
    public EconomicActivityApi provideEconomicActivityApi() {
        return getRestAdapter().create(EconomicActivityApi.class);
    }

    @Provides
    public BranchApi provideBranchApi() {
        return getRestAdapter().create(BranchApi.class);
    }

    @Provides
    public CityApi provideCityApi() {
        return getRestAdapter().create(CityApi.class);
    }

    @Provides
    public DistrictApi provideDistrictApi() {
        return getRestAdapter().create(DistrictApi.class);
    }

    @Provides
    public RegionApi provideRegionApi() {
        return getRestAdapter().create(RegionApi.class);
    }

    @Provides
    public CustomFieldApi provideCustomFieldApi() {
        return getRestAdapter().create(CustomFieldApi.class);
    }

    private RestAdapter getRestAdapter() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        return new RestAdapter
                .Builder()
                .setEndpoint(Settings.getEndpoint(app))
                .setRequestInterceptor(new ApiRequestInterceptor(app))
                .setConverter(new GsonConverter(gson))
                .build();
    }
}