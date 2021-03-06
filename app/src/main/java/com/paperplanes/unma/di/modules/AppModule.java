package com.paperplanes.unma.di.modules;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.paperplanes.unma.ResourceProvider;
import com.paperplanes.unma.ViewModelFactory;
import com.paperplanes.unma.announcementdetail.AnnouncementDetailViewModel;
import com.paperplanes.unma.announcementlist.AnnouncementListViewModel;
import com.paperplanes.unma.announcementmedialist.MediaListViewModel;
import com.paperplanes.unma.common.AuthorizationInterceptor;
import com.paperplanes.unma.common.ConnectivityInterceptor;
import com.paperplanes.unma.data.AnnouncementRepository;
import com.paperplanes.unma.data.AnnouncementRepositoryPrefs;
import com.paperplanes.unma.data.DownloadManager;
import com.paperplanes.unma.data.MemoryReactiveStore;
import com.paperplanes.unma.data.database.DatabaseAccess;
import com.paperplanes.unma.data.database.DatabaseHelper;
import com.paperplanes.unma.data.network.WebAuthentication;
import com.paperplanes.unma.data.network.WebAuthenticationLecturer;
import com.paperplanes.unma.data.network.WebAuthenticationStudent;
import com.paperplanes.unma.data.network.api.AnnouncementApi;
import com.paperplanes.unma.data.network.api.AuthenticationApi;
import com.paperplanes.unma.data.network.api.WebServiceGenerator;
import com.paperplanes.unma.data.network.api.UpdateTokenApi;
import com.paperplanes.unma.auth.SessionManager;
import com.paperplanes.unma.auth.Authentication;
import com.paperplanes.unma.AndroidSessionManager;
import com.paperplanes.unma.data.pendingtask.MarkReadTaskDatabase;
import com.paperplanes.unma.data.pendingtask.TaskDatabaseHelper;
import com.paperplanes.unma.infrastructure.DeviceConnectivityObserver;
import com.paperplanes.unma.infrastructure.PendingTaskConnectivityListener;
import com.paperplanes.unma.login.LoginViewModel;
import com.paperplanes.unma.main.MainViewModel;
import com.paperplanes.unma.model.Announcement;
import com.paperplanes.unma.data.network.api.ProfileApi;
import com.paperplanes.unma.profiledetail.ProfileDetailViewModel;
import com.paperplanes.unma.data.ProfileRepository;
import com.paperplanes.unma.data.SharedPreferencesProfileStore;
import com.paperplanes.unma.profileupdate.ProfileUpdateViewModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

/**
 * Created by abdularis on 02/11/17.
 */

@Module
public class AppModule {

    private Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    DeviceConnectivityObserver provideDeviceConnectivityObserver(Context appContext,
                                                                 AnnouncementRepository repository) {
        DeviceConnectivityObserver observer = new DeviceConnectivityObserver();
        observer.addConnectivityStateChangeListener(new PendingTaskConnectivityListener(repository));
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        appContext.registerReceiver(observer, intentFilter);
        return observer;
    }

    @Provides
    @Singleton
    SessionManager provideSession(AndroidSessionManager sessionManager) {
        return sessionManager;
    }

    @Provides
    WebAuthenticationStudent provideAuthStudentService(AuthenticationApi authApi, SessionManager sessionManager) {
        return new WebAuthenticationStudent(authApi, sessionManager);
    }

    @Provides
    WebAuthenticationLecturer provideAuthLecturerService(AuthenticationApi authApi, SessionManager sessionManager) {
        return new WebAuthenticationLecturer(authApi, sessionManager);
    }

    @Provides
    Authentication provideAuthService(WebAuthenticationStudent auth) {
        return auth;
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Context context, SessionManager sessionManager) {
        return new OkHttpClient.Builder()
                .addInterceptor(new ConnectivityInterceptor(context))
                .addInterceptor(new AuthorizationInterceptor(sessionManager))
                .build();
    }

    @Provides
    @Singleton
    WebServiceGenerator provideWebServiceGenerator(OkHttpClient httpClient) {
        return new WebServiceGenerator(httpClient);
    }

    @Provides
    @Singleton
    AuthenticationApi provideAuthApi(WebServiceGenerator webServiceGenerator) {
        return webServiceGenerator.createService(AuthenticationApi.class);
    }

    @Provides
    @Singleton
    UpdateTokenApi provideUpdateTokenApi(WebServiceGenerator webServiceGenerator) {
        return webServiceGenerator.createService(UpdateTokenApi.class);
    }

    @Provides
    @Singleton
    AnnouncementApi provideAnnouncementApi(WebServiceGenerator webServiceGenerator) {
        return webServiceGenerator.createService(AnnouncementApi.class);
    }

    @Provides
    @Singleton
    ProfileApi provideProfileApi(WebServiceGenerator webServiceGenerator) {
        return webServiceGenerator.createService(ProfileApi.class);
    }

    @Provides
    @Singleton
    DatabaseAccess provideDatabaseAccess(Context context) {
        return new DatabaseAccess(new DatabaseHelper(context));
    }

    @Provides
    @Singleton
    MarkReadTaskDatabase provideMarkReadTaskDatabase(Context context) {
        return new MarkReadTaskDatabase(new TaskDatabaseHelper(context));
    }

    @Provides
    @Singleton
    AnnouncementRepositoryPrefs provideAnnouncementRepositoryPrefs(Context context) {
        return new AnnouncementRepositoryPrefs(context);
    }

    @Provides
    @Singleton
    MemoryReactiveStore<String, Announcement> provideAnnouncementMemoryStore() {
        return new MemoryReactiveStore<>(Announcement::getId);
    }

    @Provides
    @Singleton
    AnnouncementRepository provideAnnouncementRepository(MemoryReactiveStore<String, Announcement> memoryReactive,
                                                         AnnouncementApi api,
                                                         DatabaseAccess databaseAccess,
                                                         MarkReadTaskDatabase markReadTaskDatabase,
                                                         AnnouncementRepositoryPrefs prefs) {
        return new AnnouncementRepository(memoryReactive, api, databaseAccess, markReadTaskDatabase, prefs);
    }

    @Provides
    @Singleton
    ProfileRepository provideProfileRepository(ProfileApi profileApi, Context context) {
        return new ProfileRepository(profileApi, new SharedPreferencesProfileStore(context));
    }

    @Provides
    @Singleton
    ResourceProvider provideResourceProvider(Context context) {
        return new ResourceProvider(context);
    }

    @Provides
    LoginViewModel provideLoginViewModel(ResourceProvider resourceProvider,
                                         WebAuthenticationStudent authStudent,
                                         WebAuthenticationLecturer authLecturer) {
        return new LoginViewModel(resourceProvider, authStudent, authLecturer);
    }

    @Provides
    MainViewModel provideMainViewModel(ResourceProvider resourceProvider, Authentication auth, AnnouncementRepository repository) {
        return new MainViewModel(resourceProvider, auth, repository);
    }

    @Provides
    MediaListViewModel provideMediaListViewModel(AnnouncementRepository repository,
                                                 SessionManager sessionManager,
                                                 DownloadManager downloadManager) {
        return new MediaListViewModel(repository, sessionManager, downloadManager);
    }

    @Provides
    AnnouncementListViewModel provideAnnouncementListViewModel(AnnouncementRepository repository,
                                                               SessionManager sessionManager) {
        return new AnnouncementListViewModel(repository, sessionManager);
    }

    @Provides
    ProfileDetailViewModel provideProfileDetailViewModel(ProfileRepository repository, SessionManager sessionManager) {
        return new ProfileDetailViewModel(repository, sessionManager);
    }

    @Provides
    ProfileUpdateViewModel provideProfileUpdateViewModel(ResourceProvider resourceProvider, ProfileRepository repository, SessionManager sessionManager) {
        return new ProfileUpdateViewModel(resourceProvider, repository, sessionManager);
    }

    @Provides
    @Singleton
    DownloadManager provideDownloadManager(Context context, DatabaseAccess databaseAccess) {
        return new DownloadManager(context, databaseAccess);
    }

    @Provides
    AnnouncementDetailViewModel provideAnnouncementDetailViewModel(AnnouncementRepository repository,
                                                                   DownloadManager downloadManager) {
        return new AnnouncementDetailViewModel(repository, downloadManager);
    }

    @Provides
    @Singleton
    ViewModelProvider.Factory provideViewModelFactory(ViewModelFactory modelFactory) {
        return modelFactory;
    }
}
