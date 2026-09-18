package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinotaurExplorer.class, Mountain.class})
class MinotaurExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB with a card in hand prompts the may ability choice")
    void etbWithCardInHandPromptsMayAbility() {
        castMinotaurExplorerWithCardInHand();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting discards a card at random and keeps Minotaur Explorer")
    void acceptingDiscardsAtRandomAndKeepsMinotaurExplorer() {
        castMinotaurExplorerWithCardInHand();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Minotaur Explorer");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting with multiple cards discards exactly one at random")
    void acceptingWithMultipleCardsDiscardsExactlyOneAtRandom() {
        harness.castFromHand(player1, new MinotaurExplorer(), "{1}{R}");
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Minotaur Explorer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining sacrifices Minotaur Explorer and leaves the hand untouched")
    void decliningSacrificesMinotaurExplorer() {
        castMinotaurExplorerWithCardInHand();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Minotaur Explorer");
        harness.assertInGraveyard(player1, "Minotaur Explorer");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Auto-sacrifices with no card to discard")
    void autoSacrificesWithEmptyHand() {
        harness.castFromHand(player1, new MinotaurExplorer(), "{1}{R}");
        harness.setHand(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Minotaur Explorer");
        harness.assertInGraveyard(player1, "Minotaur Explorer");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castMinotaurExplorerWithCardInHand() {
        harness.castFromHand(player1, new MinotaurExplorer(), "{1}{R}");
        harness.setHand(player1, List.of(new Mountain()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
