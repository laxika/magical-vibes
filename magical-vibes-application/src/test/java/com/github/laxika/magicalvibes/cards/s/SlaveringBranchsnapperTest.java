package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlaveringBranchsnapper.class, Forest.class, GrizzlyBears.class})
class SlaveringBranchsnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Forestcycling discards the card and searches for a Forest")
    void forestcyclingDiscardsAndSearchesForForest() {
        harness.setHand(player1, List.of(new SlaveringBranchsnapper()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Slavering Branchsnapper");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Forest)
                .hasSize(1);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }
    @Test
    @DisplayName("Forestcycling pays its discard cost before the search resolves and reveals the Forest")
    void discardIsPaidBeforeResolutionAndForestIsRevealed() {
        harness.setHand(player1, List.of(new SlaveringBranchsnapper()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Slavering Branchsnapper");
        harness.assertNotInHand(player1, "Slavering Branchsnapper");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling may fail to find an available Forest")
    void mayDeclineAvailableForest() {
        harness.setHand(player1, List.of(new SlaveringBranchsnapper()));
        harness.setLibrary(player1, List.of(new Forest(), new SlaveringBranchsnapper()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Slavering Branchsnapper");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling does not draw a card when the library contains no Forest")
    void noForestDoesNotDraw() {
        harness.setHand(player1, List.of(new SlaveringBranchsnapper()));
        harness.setLibrary(player1, List.of(new SlaveringBranchsnapper()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Slavering Branchsnapper");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana prevents Forestcycling without discarding the card")
    void insufficientManaDoesNotDiscard() {
        harness.setHand(player1, List.of(new SlaveringBranchsnapper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Slavering Branchsnapper");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
