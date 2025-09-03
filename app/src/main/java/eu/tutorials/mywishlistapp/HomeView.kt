package eu.tutorials.mywishlistapp

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.tutorials.mywishlistapp.data.Wish
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeView(
    navController: NavController,
    viewModel: WishViewModel
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val query by viewModel.searchQuery.collectAsState()
    val sort by viewModel.sortOption.collectAsState()
    val wishlist by viewModel.visibleWishes.collectAsState(emptyList())

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBarView(title = "WishList") },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(all = 20.dp),
                contentColor = Color.White,
                backgroundColor = Color.Black,
                onClick = {
                    navController.navigate(Screen.AddScreen.route + "/0")
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search + Sort Row
            SearchAndSortRow(
                query = query,
                onQueryChange = viewModel::onSearchQueryChanged,
                sort = sort,
                onSortChange = viewModel::onSortOptionChanged
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (wishlist.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No wishes yet. Tap + to add one.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(
                        items = wishlist,
                        key = { it.id }
                    ) { wish ->
                        val dismissState = rememberDismissState(
                            confirmStateChange = { state ->
                                if (state == DismissValue.DismissedToStart ||
                                    state == DismissValue.DismissedToEnd
                                ) {
                                    // Delete with undo option
                                    scope.launch {
                                        viewModel.deleteWishWithUndo(wish)
                                        val result = scaffoldState.snackbarHostState.showSnackbar(
                                            message = "Wish deleted",
                                            actionLabel = "UNDO",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == androidx.compose.material.SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
                                        }
                                    }
                                }
                                true
                            }
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            background = {
                                val color by animateColorAsState(
                                    if (dismissState.dismissDirection == DismissDirection.EndToStart ||
                                        dismissState.dismissDirection == DismissDirection.StartToEnd
                                    ) Color.Red else Color.Transparent,
                                    label = ""
                                )
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            },
                            directions = setOf(
                                DismissDirection.EndToStart,
                                DismissDirection.StartToEnd
                            ),
                            dismissContent = {
                                WishItem(wish = wish) {
                                    navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchAndSortRow(
    query: String,
    onQueryChange: (String) -> Unit,
    sort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    val sortExpanded = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            label = { Text("Search") },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = colorResource(id = R.color.black),
                unfocusedBorderColor = colorResource(id = R.color.black),
                textColor = Color.Black,
                cursorColor = colorResource(id = R.color.black)
            )
        )

        Box {
            IconButton(onClick = { sortExpanded.value = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (sort) {
                            SortOption.NEWEST -> "Newest"
                            SortOption.TITLE_ASC -> "A–Z"
                            SortOption.TITLE_DESC -> "Z–A"
                        }
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort")
                }
            }
            DropdownMenu(
                expanded = sortExpanded.value,
                onDismissRequest = { sortExpanded.value = false }
            ) {
                DropdownMenuItem(onClick = {
                    onSortChange(SortOption.NEWEST)
                    sortExpanded.value = false
                }) { Text("Newest") }

                DropdownMenuItem(onClick = {
                    onSortChange(SortOption.TITLE_ASC)
                    sortExpanded.value = false
                }) { Text("A–Z") }

                DropdownMenuItem(onClick = {
                    onSortChange(SortOption.TITLE_DESC)
                    sortExpanded.value = false
                }) { Text("Z–A") }
            }
        }
    }
}

@Composable
fun WishItem(wish: Wish, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            .clickable { onClick() },
        elevation = 8.dp,
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = wish.title, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = wish.description)
        }
    }
}
