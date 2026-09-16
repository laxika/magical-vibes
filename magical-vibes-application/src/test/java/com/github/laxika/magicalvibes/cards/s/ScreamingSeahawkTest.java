package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarrenMoor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamingSeahawk.class, BarrenMoor.class})
class ScreamingSeahawkTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may search prompt")
    void enteringTheBattlefieldCreatesMaySearchPrompt() {
        setupAndCast();

        resolveCreatureAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability searches for a Screaming Seahawk")
    void acceptingMaySearchesForScreamingSeahawk() {
        setupAndCast();
        ScreamingSeahawk seahawk = new ScreamingSeahawk();
        BarrenMoor barrenMoor = new BarrenMoor();
        harness.setLibrary(player1, List.of(seahawk, barrenMoor));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(seahawk);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(seahawk);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(barrenMoor);
        assertThat(gameLogContains("reveals Screaming Seahawk")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Accepting the search moves only the chosen matching copy")
    void acceptingSearchMovesOnlyChosenMatchingCopy() {
        setupAndCast();
        ScreamingSeahawk firstSeahawk = new ScreamingSeahawk();
        ScreamingSeahawk secondSeahawk = new ScreamingSeahawk();
        BarrenMoor barrenMoor = new BarrenMoor();
        harness.setLibrary(player1, List.of(firstSeahawk, secondSeahawk, barrenMoor));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(firstSeahawk, secondSeahawk);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondSeahawk);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstSeahawk, barrenMoor);
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningMayDoesNotSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new ScreamingSeahawk()));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The search offers only cards named Screaming Seahawk")
    void searchFiltersByName() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new ScreamingSeahawk(), new BarrenMoor()));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(1)
                .allMatch(card -> card.getName().equals("Screaming Seahawk"));
    }

    @Test
    @DisplayName("An empty search has no eligible cards")
    void emptySearchHasNoEligibleCards() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new BarrenMoor()));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("finds no cards named Screaming Seahawk")).isTrue();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new ScreamingSeahawk(), "{4}{U}");
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
