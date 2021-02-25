# weatherApp


main feature:
- user can search with city name to get weather data about this city 
- user can get weather data by accessing GPS 
- user can change degree from Celsius to Fahrenheit by using top menu
- Weather data (city degree - pressure - feels like )

#: Live Demo:

    https://drive.google.com/file/d/1SRniMyT_zq-D6hxbPSA0GkhviO0RfFmR/view?usp=sharing
    
    

# Tools & libraries 

- Language: Kotlin
- Design pattern : MVVM 
- LiveData
- Goole maps GPS checker dialog 
- https://openweathermap.org/api to get the weather data
- https://openweathermap.org/current   by query & By geographic coordinates



#: Required Dependencies:
    def lifecycle_version = "2.2.0"
    implementation 'com.squareup.retrofit2:retrofit:2.9.0' // API calling
    implementation 'com.google.code.gson:gson:2.8.6'  // JSON parsing
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.0'
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-extensions:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
    implementation 'com.google.android.gms:play-services-location:17.1.0'
    
    

    screenshots
[url=https://postimg.cc/rKq42hkx][img]https://i.postimg.cc/rKq42hkx/Screenshot-20210225-122638-Copy.jpg[/img][/url]

[url=https://postimg.cc/ppkFWDW7][img]https://i.postimg.cc/ppkFWDW7/Screenshot-20210225-122648.jpg[/img][/url]

[url=https://postimg.cc/McfMmN72][img]https://i.postimg.cc/McfMmN72/Screenshot-20210225-122705.jpg[/img][/url]

[url=https://postimg.cc/870fWNrm][img]https://i.postimg.cc/870fWNrm/Screenshot-20210225-122746.jpg[/img][/url]




