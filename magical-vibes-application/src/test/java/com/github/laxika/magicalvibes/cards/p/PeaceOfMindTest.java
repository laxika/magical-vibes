package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AetherTide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeaceOfMind.class, AetherTide.class})
class PeaceOfMindTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability starts a discard-cost choice for any card")
    void activationStartsDiscardChoice() {
        harness.addToBattlefield(player1, new PeaceOfMind());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new AetherTide()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    @DisplayName("Discarding a card gains 3 life on resolution")
    void gains3LifeOnResolution() {
        harness.addToBattlefield(player1, new PeaceOfMind());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new AetherTide()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        // Discard was paid as a cost
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Aether Tide");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities(); // resolve the ability

        harness.assertLife(player1, lifeBefore + 3);
    }

    @Test
    @DisplayName("Cannot activate without a card in hand to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new PeaceOfMind());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new PeaceOfMind());
        harness.setHand(player1, List.of(new AetherTide()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
