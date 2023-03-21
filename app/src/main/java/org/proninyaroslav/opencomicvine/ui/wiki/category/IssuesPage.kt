package org.proninyaroslav.opencomicvine.ui.wiki.category

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.util.Pair
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import org.proninyaroslav.opencomicvine.R
import org.proninyaroslav.opencomicvine.data.preferences.PrefWikiIssuesFilter
import org.proninyaroslav.opencomicvine.data.preferences.PrefWikiIssuesFilterBundle
import org.proninyaroslav.opencomicvine.ui.components.DateRangePickerDialog
import org.proninyaroslav.opencomicvine.ui.components.card.IssueCard
import org.proninyaroslav.opencomicvine.ui.components.list.CardCellSize
import org.proninyaroslav.opencomicvine.ui.components.list.EmptyListPlaceholder
import org.proninyaroslav.opencomicvine.ui.viewmodel.*
import org.proninyaroslav.opencomicvine.ui.wiki.WikiPage
import org.proninyaroslav.opencomicvine.ui.wiki.category.filter.*
import java.util.*

@Composable
fun IssuesPage(
    viewModel: WikiCategoryPageViewModel,
    networkConnection: NetworkConnectionViewModel,
    datePickerViewModel: DatePickerViewModel,
    filterViewModel: IssuesFilterViewModel,
    favoritesViewModel: FavoritesViewModel,
    onLoadPage: (WikiPage) -> Unit,
    onBackButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val issues = viewModel.issuesList.collectAsLazyPagingItems()
    val filterState by filterViewModel.state.collectAsStateWithLifecycle()
    val showApplyButton by remember(filterState) {
        derivedStateOf {
            when (val s = filterState) {
                is IssuesFilterState.SortChanged -> s.isNeedApply
                is IssuesFilterState.FiltersChanged -> s.isNeedApply
                else -> false
            }
        }
    }
    val datePickerState by datePickerViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(filterViewModel) {
        filterViewModel.effect.collect { effect ->
            when (effect) {
                IssuesFilterEffect.Applied -> issues.refresh()
            }
        }
    }

    DateRangePickerDialog(
        show = when (datePickerState) {
            DatePickerState.Hide,
            DatePickerState.Initial -> false
            is DatePickerState.Show<*> -> true
        },
        titleText = R.string.date_picker_select_dates,
        initRange = when (val s = datePickerState) {
            DatePickerState.Hide,
            DatePickerState.Initial -> null
            is DatePickerState.Show<*> -> s.range
        },
        onPositiveClicked = { selection ->
            when (val s = datePickerState) {
                DatePickerState.Hide -> {}
                DatePickerState.Initial -> {}
                is DatePickerState.Show<*> -> {
                    val filter =
                        handleDatePickerResult(
                            filterState = filterState,
                            type = s.dialogType as IssuesDatePickerType,
                            selection = selection,
                        )
                    filter?.let {
                        filterViewModel.event(
                            IssuesFilterEvent.ChangeFilters(
                                filterBundle = filter,
                            )
                        )
                    }
                }
            }
        },
        onHide = { datePickerViewModel.event(DatePickerEvent.Hide) },
    )

    WikiCategoryPage(
        type = WikiCategoryPageType.Issues,
        title = { Text(stringResource(R.string.issues)) },
        itemCard = { item ->
            IssueCard(
                issueInfo = item.info,
                onClick = { onLoadPage(WikiPage.Issue(item.id)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        emptyListPlaceholder = {
            EmptyListPlaceholder(
                icon = R.drawable.ic_menu_book_24,
                label = stringResource(R.string.no_issues),
            )
        },
        filterDrawerContent = {
            issuesFilter(
                sort = filterState.sort,
                filterBundle = filterState.filterBundle,
                onSortChanged = {
                    filterViewModel.event(
                        IssuesFilterEvent.ChangeSort(sort = it)
                    )
                },
                onFiltersChanged = {
                    filterViewModel.event(
                        IssuesFilterEvent.ChangeFilters(filterBundle = it)
                    )
                },
                onDatePickerDialogShow = { type, date ->
                    datePickerViewModel.event(
                        DatePickerEvent.Show(
                            dialogType = type,
                            range = date.run { Pair(first.time, second.time) }
                        )
                    )
                },
            )
        },
        items = issues,
        errorMessageTemplates = WikiCategoryPage.ErrorMessageTemplates(
            fetchTemplate = R.string.fetch_issues_list_error_template,
            saveTemplate = R.string.cache_issues_list_error_template,
        ),
        cellSize = CardCellSize.Adaptive.Large,
        showApplyButton = showApplyButton,
        onApplyFilter = { filterViewModel.event(IssuesFilterEvent.Apply) },
        viewModel = viewModel,
        networkConnection = networkConnection,
        favoritesViewModel = favoritesViewModel,
        onBackButtonClicked = onBackButtonClicked,
        modifier = modifier,
    )
}

private fun handleDatePickerResult(
    filterState: IssuesFilterState,
    type: IssuesDatePickerType,
    selection: Pair<Long, Long>,
): PrefWikiIssuesFilterBundle? =
    when (type) {
        IssuesDatePickerType.Unknown -> null
        IssuesDatePickerType.DateAdded -> filterState.filterBundle.copy(
            dateAdded = PrefWikiIssuesFilter.DateAdded.InRange(
                start = Date(selection.first),
                end = Date(selection.second),
            ),
        )
        IssuesDatePickerType.DateLastUpdated -> filterState.filterBundle.copy(
            dateLastUpdated = PrefWikiIssuesFilter.DateLastUpdated.InRange(
                start = Date(selection.first),
                end = Date(selection.second),
            ),
        )
        IssuesDatePickerType.CoverDate -> filterState.filterBundle.copy(
            coverDate = PrefWikiIssuesFilter.CoverDate.InRange(
                start = Date(selection.first),
                end = Date(selection.second),
            ),
        )
        IssuesDatePickerType.StoreDate -> filterState.filterBundle.copy(
            storeDate = PrefWikiIssuesFilter.StoreDate.InRange(
                start = Date(selection.first),
                end = Date(selection.second),
            ),
        )
    }