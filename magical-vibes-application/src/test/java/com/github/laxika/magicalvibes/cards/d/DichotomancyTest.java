package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dichotomancy.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class DichotomancyTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the target opponent's library for each tapped nonland permanent")
    void searchesForTappedNonlandPermanentNames() {
        Permanent tappedBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedBears.tap();
        Permanent tappedElves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        tappedElves.tap();
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent tappedForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        tappedForest.tap();

        Card libraryBears = new GrizzlyBears();
        Card libraryElves = new LlanowarElves();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(libraryBears, libraryElves));

        castDichotomancy();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch).isNotNull();
        assertThat(firstSearch.params().playerId()).isEqualTo(player1.getId());
        assertThat(firstSearch.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(firstSearch.params().battlefieldControllerId()).isEqualTo(player1.getId());
        assertThat(firstSearch.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch secondSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch).isNotNull();
        assertThat(secondSearch.params().cards()).extracting(Card::getName)
                .containsExactly("Llanowar Elves");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only tapped nonlands create searches")
    void ignoresUntappedPermanentsAndLands() {
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new LlanowarElves());

        castDichotomancy();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new Dichotomancy()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Suspend exiles Dichotomancy with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        Dichotomancy card = new Dichotomancy();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    private void castDichotomancy() {
        harness.setHand(player1, List.of(new Dichotomancy()));
        harness.addMana(player1, ManaColor.BLUE, 9);
        harness.castSorcery(player1, 0, player2.getId());
    }
}
