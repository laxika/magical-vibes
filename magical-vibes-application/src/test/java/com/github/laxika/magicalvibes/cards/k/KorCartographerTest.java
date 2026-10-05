package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KorCartographer.class, Plains.class, Forest.class})
class KorCartographerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put a Plains onto the battlefield tapped")
    void mayPutPlainsOntoBattlefieldTapped() {
        castKorCartographer();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Plains"));

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB search does not put a Plains onto the battlefield")
    void decliningSearchDoesNothing() {
        castKorCartographer();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("The search may fail to find even when a Plains is available")
    void mayFindNoCardWithPlainsAvailable() {
        castKorCartographer();
        setupLibrary();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the search with no Plains leaves the library intact")
    void searchWithNoPlains() {
        castKorCartographer();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the search with an empty library completes the ability")
    void searchWithEmptyLibrary() {
        castKorCartographer();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Kor Cartographer");
    }

    private void castKorCartographer() {
        harness.castFromHand(player1, new KorCartographer(), "{3}{W}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
    }
}
