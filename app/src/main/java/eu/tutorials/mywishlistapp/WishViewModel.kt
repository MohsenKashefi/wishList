package eu.tutorials.mywishlistapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.Wish
import eu.tutorials.mywishlistapp.data.WishRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SortOption { NEWEST, TITLE_ASC, TITLE_DESC }

class WishViewModel(
    private val wishRepository: WishRepository = Graph.wishRepository
) : ViewModel() {

    // Form state as flows
    private val _wishTitleState = MutableStateFlow("")
    val wishTitleState: StateFlow<String> = _wishTitleState.asStateFlow()

    private val _wishDescriptionState = MutableStateFlow("")
    val wishDescriptionState: StateFlow<String> = _wishDescriptionState.asStateFlow()

    fun onWishTitleChanged(newString: String) {
        _wishTitleState.value = newString
    }

    fun onWishDescriptionChanged(newString: String) {
        _wishDescriptionState.value = newString
    }

    // Search / Sort
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    fun onSearchQueryChanged(q: String) {
        _searchQuery.value = q
    }

    fun onSortOptionChanged(option: SortOption) {
        _sortOption.value = option
    }

    // Source of truth
    private val allWishes: Flow<List<Wish>> = wishRepository.getWishes()

    // Visible list (search + sort applied)
    val visibleWishes: Flow<List<Wish>> = combine(allWishes, _searchQuery, _sortOption) { list, q, sort ->
        val filtered = if (q.isBlank()) list else {
            list.filter {
                it.title.contains(q, ignoreCase = true) ||
                        it.description.contains(q, ignoreCase = true)
            }
        }
        when (sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.id } // id as proxy for newest
            SortOption.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
        }
    }

    // CRUD
    fun addWish(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            wishRepository.addAWish(wish)
            clearForm()
        }
    }

    fun updateWish(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            wishRepository.updateAWish(wish)
            clearForm()
        }
    }

    fun getAWishById(id: Long): Flow<Wish> = wishRepository.getAWishById(id)

    // Populate form when editing
    fun populateForEdit(id: Long) {
        viewModelScope.launch {
            if (id == 0L) {
                clearForm()
            } else {
                val existing = getAWishById(id).first()
                _wishTitleState.value = existing.title
                _wishDescriptionState.value = existing.description
            }
        }
    }

    private fun clearForm() {
        _wishTitleState.value = ""
        _wishDescriptionState.value = ""
    }

    // Delete with undo support
    private var lastDeleted: Wish? = null

    fun deleteWishWithUndo(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            lastDeleted = wish
            wishRepository.deleteAWish(wish)
        }
    }

    fun undoDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            lastDeleted?.let { wishRepository.addAWish(it) }
            lastDeleted = null
        }
    }
}
