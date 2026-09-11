package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ephemeron.class, RagingGoblin.class, Spellbook.class})
class EphemeronTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card puts Ephemeron's return ability on the stack")
    void discardCostPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new Ephemeron());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Discarding a card returns Ephemeron to its owner's hand")
    void discardCostReturnsEphemeronToHand() {
        harness.addToBattlefield(player1, new Ephemeron());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInHand(player1, "Ephemeron");
        harness.assertNotOnBattlefield(player1, "Ephemeron");
    }

    @Test
    @DisplayName("Activation lets the controller choose exactly one card from hand")
    void activationOffersEachHandCardAndDiscardsChosenCard() {
        harness.addToBattlefield(player1, new Ephemeron());
        harness.setHand(player1, List.of(new RagingGoblin(), new Spellbook()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Raging Goblin");
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Raging Goblin");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ephemeron cannot activate without a card in hand")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefield(player1, new Ephemeron());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving returns an Ephemeron controlled by another player to its owner's hand")
    void discardCostReturnsEphemeronToOwnersHand() {
        Ephemeron ephemeron = new Ephemeron();
        ephemeron.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, ephemeron);
        harness.setHand(player2, List.of(new Spellbook()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertInHand(player1, "Ephemeron");
        harness.assertNotInHand(player2, "Ephemeron");
        harness.assertNotOnBattlefield(player2, "Ephemeron");
    }
}
