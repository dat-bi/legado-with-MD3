# Discover / Book Info Screen Implementation Guide

This document maps where the "Discover" (Khám phá) and "Book Info" (Thông tin truyện) UI sections load content in the legado application.

## 1. DISCOVER / EXPLORATION SECTION (Khám phá)

### Main Fragment

**File:** [app/src/main/java/io/legado/app/ui/main/explore/ExploreFragment.kt](app/src/main/java/io/legado/app/ui/main/explore/ExploreFragment.kt)

- **Class:** `ExploreFragment`
- **Key Methods:**
  - [Line 48](app/src/main/java/io/legado/app/ui/main/explore/ExploreFragment.kt#L48): Class declaration
  - [Line 89-143](app/src/main/java/io/legado/app/ui/main/explore/ExploreFragment.kt#L89-L143): `upExploreData()` - Loads explore data by querying book sources
    - Fetches explore rules from database via `appDb.bookSourceDao.flowExplore()`
    - Filters by search query or group
    - Updates adapter with book sources

### ViewModel

**File:** [app/src/main/java/io/legado/app/ui/main/explore/ExploreViewModel.kt](app/src/main/java/io/legado/app/ui/main/explore/ExploreViewModel.kt)

- **Class:** `ExploreViewModel`
- Handles source ordering and deletion
- Simple model for managing explore sources

### Adapter / Display

**File:** [app/src/main/java/io/legado/app/ui/main/explore/ExploreAdapter.kt](app/src/main/java/io/legado/app/ui/main/explore/ExploreAdapter.kt)

- **Class:** `ExploreAdapter`
- [Line 68-90](app/src/main/java/io/legado/app/ui/main/explore/ExploreAdapter.kt#L68-L90): `convert()` - Renders each explore source item
  - Calls `item.exploreKinds()` to load explore categories asynchronously
  - [Line 86-90](app/src/main/java/io/legado/app/ui/main/explore/ExploreAdapter.kt#L86-L90): Loads explore kinds (categories)
- [Line 93-121](app/src/main/java/io/legado/app/ui/main/explore/ExploreAdapter.kt#L93-L121): `upKindList()` - Displays explore kinds as clickable buttons

### Explore Show Screen (Browse books in a category)

**File:** [app/src/main/java/io/legado/app/ui/book/explore/ExploreShowActivity.kt](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowActivity.kt)

- **Class:** `ExploreShowActivity`
- [Line 23-24](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowActivity.kt#L23-L24): Launches `ExploreShowScreen` with sourceUrl and exploreUrl

### Explore Show Screen Composable

**File:** [app/src/main/java/io/legado/app/ui/book/explore/ExploreShowScreen.kt](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowScreen.kt)

- **Function:** `ExploreShowScreen()`
- [Line 100-102](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowScreen.kt#L100-L102): `LaunchedEffect()` - Calls `viewModel.initData(intent)`
- [Line 104-142](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowScreen.kt#L104-L142): Collects UI state flows
  - `viewModel.uiBooks` - List of discovered books
  - `viewModel.isEnd` - Pagination flag
  - `viewModel.kinds` - Available explore categories
  - `viewModel.isLoading` - Loading state
  - `viewModel.errorMsg` - Error messages

### Explore Show ViewModel

**File:** [app/src/main/java/io/legado/app/ui/book/explore/ExploreShowViewModel.kt](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowViewModel.kt)

- **Class:** `ExploreShowViewModel`
- [Line 168-177](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowViewModel.kt#L168-L177): `initData()` - Initializes explore
  - Gets book source from intent
  - Gets explore kinds for the source
  - Loads books via `loadMore(isRefresh = true)`
- [Line 188-219](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowViewModel.kt#L188-L219): `loadMore()` - **KEY METHOD FOR LOADING BOOKS**
  - Calls `repository.exploreBook(source, url, page)` to fetch books from source
  - **This is where you can add translation functionality**
  - Saves books to database via `repository.saveSearchBooks(newBooks)`
  - Updates UI state flows

---

## 2. BOOK INFO / DETAIL SCREEN (Thông tin truyện)

### Main Activity

**File:** [app/src/main/java/io/legado/app/ui/book/info/BookInfoActivity.kt](app/src/main/java/io/legado/app/ui/book/info/BookInfoActivity.kt)

- **Class:** `BookInfoActivity`
- [Line 148-170](app/src/main/java/io/legado/app/ui/book/info/BookInfoActivity.kt#L148-L170): Activity initialization
  - Receives book data from intent or search results
  - Displays book cover, title, author, description

### ViewModel

**File:** [app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt)

- **Class:** `BookInfoViewModel`
- **Key Methods:**

#### Load Book Info

[Line 108-158](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L108-L158): `loadBookInfo()` - **PRIMARY CONTENT LOADING**

- Handles both local and online books
- **For Online Books:**
  - Calls `WebBook.getBookInfo()` to fetch book details (title, author, description, etc.)
  - **This is a key place to add book detail translation**
- **For Local Books:**
  - Calls `LocalBook.upBookInfo()` to read local file metadata

[Line 160-201](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L160-L201): `loadChapter()` - **LOADS TABLE OF CONTENTS**

- **For Online Books:**
  - Calls `WebBook.getChapterList()` to fetch all chapters
  - **This is where you could add chapter title translation**
  - Updates database with chapter list
  - Posts chapter data to UI via `chapterListData.postValue(it)`
- **For Local Books:**
  - Calls `LocalBook.getChapterList()`

#### Other Loading Methods

[Line 62-82](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L62-L82): `upBook()` - Initializes book data

- Sets book cover, loads metadata
- Calls `loadBookInfo()` for online books
- Calls `loadChapter()` for chapter list

[Line 84-95](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L84-L95): `upCoverByRule()` - Searches and updates book cover

[Line 202-217](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L202-L217): `loadWebFile()` - Loads downloadable web files if applicable

---

## 3. CONTENT LOADING PIPELINE

### For Discover Screen:

```
ExploreFragment.upExploreData()
  ↓
appDb.bookSourceDao.flowExplore()  [Get book sources with explore rules]
  ↓
ExploreAdapter.convert()
  ↓
ExploreShowActivity (when user clicks a category)
  ↓
ExploreShowScreen
  ↓
ExploreShowViewModel.initData()
  ↓
ExploreShowViewModel.loadMore()  ← ADD TRANSLATION HERE
  ↓
repository.exploreBook(source, url, page)  [Fetch books from source]
  ↓
repository.saveSearchBooks(newBooks)  [Save to database]
  ↓
Display books in UI (with potential for book title/author translation)
```

### For Book Info Screen:

```
BookInfoActivity
  ↓
BookInfoViewModel.initData() / upBook()
  ↓
BookInfoViewModel.loadBookInfo()  ← ADD BOOK DETAIL TRANSLATION HERE
  ↓
WebBook.getBookInfo() [Fetch title, author, description, cover, etc.]
  ↓
Update bookData LiveData
  ↓
BookInfoViewModel.loadChapter()  ← ADD CHAPTER TITLE TRANSLATION HERE
  ↓
WebBook.getChapterList() [Fetch all chapters]
  ↓
Update chapterListData LiveData
  ↓
Display in UI
```

---

## 4. TRANSLATION INTEGRATION POINTS

### Discover Screen Translation Points:

1. **Book Title & Author Translation**
   - File: [ExploreShowViewModel.kt#L188-L219](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowViewModel.kt#L188-L219)
   - In `loadMore()` method, after `repository.exploreBook()` returns `newBooks`
   - Add translation to each `SearchBook` item's `name` and `author` fields

2. **Explore Kind (Category) Translation**
   - File: [ExploreShowViewModel.kt#L179-184](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowViewModel.kt#L179-L184)
   - In `loadKinds()` method, after `repository.getSourceExploreKinds(sourceUrl)`
   - Translate category titles in `ExploreKind` items

### Book Info Screen Translation Points:

1. **Book Title & Author Translation**
   - File: [BookInfoViewModel.kt#L108-158](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L108-L158)
   - In `loadBookInfo()` method
   - After `WebBook.getBookInfo()` completes, translate book's `name` and `author`

2. **Book Description Translation**
   - Same location: [BookInfoViewModel.kt#L108-158](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L108-L158)
   - Translate book's `intro` (description) field

3. **Chapter Title Translation**
   - File: [BookInfoViewModel.kt#L160-201](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L160-L201)
   - In `loadChapter()` method
   - After `WebBook.getChapterList()` returns chapters
   - Translate each chapter's `title` field in the chapter list

---

## 5. KEY CLASSES & ENTITIES

- **SearchBook**: Represents a book found in explore/search results
- **Book**: Main book entity with detailed information
- **BookChapter**: Chapter entity with title and URL
- **ExploreKind**: Category/kind in an explore rule
- **BookSource**: Book source configuration with explore/search rules
- **TranslateManager**: Available translation utility (reference for implementation)

---

## 6. REPOSITORY METHODS

**File:** Data access layer in repository classes

- `repository.exploreBook(source, url, page)` - Fetches books from explore URL
- `repository.saveSearchBooks(books)` - Persists books to database
- `repository.getSourceExploreKinds(sourceUrl)` - Gets explore categories
- `WebBook.getBookInfo()` - Fetches book metadata
- `WebBook.getChapterList()` - Fetches chapter list

---

## Summary

The two main screens that need translation integration are:

1. **Discover Screen**: [ExploreShowViewModel.loadMore()](app/src/main/java/io/legado/app/ui/book/explore/ExploreShowViewModel.kt#L188-L219) - Translates book titles/authors when fetched
2. **Book Info Screen**: [BookInfoViewModel.loadBookInfo()](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L108-L158) and [loadChapter()](app/src/main/java/io/legado/app/ui/book/info/BookInfoViewModel.kt#L160-L201) - Translates book details and chapter titles

Both locations should call the existing `TranslateManager` after fetching content to apply translations before displaying to the user.
