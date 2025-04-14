🥕 Zöldség-Gyümölcs Rendelő Mobilalkalmazás

Ez a projekt egy Java alapú Android mobilalkalmazás, amely lehetővé teszi a felhasználók számára, hogy zöldségeket és gyümölcsöket rendeljenek egy online piactéren keresztül. Az alkalmazás Firebase Authentication-t és Firestore adatbázist használ a felhasználói fiókok és kosárkezelés kezelésére.
📱 Főbb funkciók

    ✅ Bejelentkezés / Regisztráció

    ✅ Terméklista megtekintése (pl. alma, répa, citrom, káposzta stb.)

    ✅ Kosár funkció – mennyiséggel együtt

    ✅ Rendelési összeg kiszámítása

    ✅ Menüfunkció: kijelentkezés, navigáció

🛠️ Technológiák

    Android (Java)

    Firebase Authentication
    Firebase Firestore Database

    ConstraintLayout + RecyclerView

📁 Projekt felépítése

    MainActivity – nyitóképernyő, átirányítás bejelentkezéshez vagy regisztrációhoz
    LoginActivity – bejelentkezési felület
    RegisterActivity – regisztrációs felület
    HomeActivity – piactér, termékek listázása
    CartActivity – kosár megtekintése

    ProductAdapter.java – RecyclerView adapter, kosárkezeléssel
    Product.java – termék adatmodell

    res/layout/ – XML UI fájlok
    res/values/colors.xml – app színtémák

🔒 Firebase funkciók

    Felhasználók hitelesítése (email/jelszó)
    Kosártartalom mentése: users/{uid}/cart
    Quantity alapú kosárkezelés
    Realtime kosárfrissítés

👨‍💻 Készítette

Szombati Konrád (IJKCKR)
Mobil Alkalmazásfejlesztés beadandó
2024/25. tavaszi félév – Android (Java) + Firebase