package eu.tutorials.mywishlistapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.tutorials.mywishlistapp.data.Wish
import kotlinx.coroutines.launch

@Composable
fun AddEditDetailView(
    id: Long,
    viewModel: WishViewModel,
    navController: NavController
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    // Observe states from ViewModel
    val title by viewModel.wishTitleState.collectAsState()
    val description by viewModel.wishDescriptionState.collectAsState()

    // Load wish once if editing
    LaunchedEffect(id) {
        viewModel.populateForEdit(id)
    }

    Scaffold(
        topBar = {
            AppBarView(
                title = if (id != 0L) stringResource(id = R.string.update_wish)
                else stringResource(id = R.string.add_wish),
                onBackNavClicked = { navController.navigateUp() }
            )
        },
        scaffoldState = scaffoldState
    ) { padding ->
        // ✅ Fetch strings in composable scope
        val fillAllFieldsMsg = stringResource(id = R.string.fill_all_fields)
        val createdMsg = stringResource(id = R.string.wish_created)
        val updatedMsg = stringResource(id = R.string.wish_updated)

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            WishTextField(
                label = stringResource(id = R.string.title_label),
                value = title,
                onValueChanged = { viewModel.onWishTitleChanged(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            WishTextField(
                label = stringResource(id = R.string.description_label),
                value = description,
                onValueChanged = { viewModel.onWishDescriptionChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            if (id != 0L) {
                                viewModel.updateWish(
                                    Wish(
                                        id = id,
                                        title = title.trim(),
                                        description = description.trim()
                                    )
                                )
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = updatedMsg,
                                    duration = SnackbarDuration.Short
                                )
                            } else {
                                viewModel.addWish(
                                    Wish(
                                        title = title.trim(),
                                        description = description.trim()
                                    )
                                )
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = createdMsg,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            navController.navigateUp()
                        } else {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = fillAllFieldsMsg,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            ) {
                Text(
                    text = if (id != 0L) stringResource(id = R.string.update_wish)
                    else stringResource(id = R.string.add_wish),
                    style = TextStyle(fontSize = 18.sp)
                )
            }
        }
    }
}

@Composable
fun WishTextField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(text = label, color = Color.Black) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color.Black,
            focusedBorderColor = colorResource(id = R.color.black),
            unfocusedBorderColor = colorResource(id = R.color.black),
            cursorColor = colorResource(id = R.color.black),
            focusedLabelColor = colorResource(id = R.color.black),
            unfocusedLabelColor = colorResource(id = R.color.black),
        )
    )
}

@Preview
@Composable
fun WishTextFieldPrev() {
    WishTextField(label = "Text", value = "Text") {}
}
