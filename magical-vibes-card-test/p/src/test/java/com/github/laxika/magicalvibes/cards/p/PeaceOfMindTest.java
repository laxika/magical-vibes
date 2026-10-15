package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeaceOfMind.class, HonorGuard.class})
class PeaceOfMindTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability starts a discard-cost choice for any card")
    void activationStartsDiscardChoice() {
        harness.addToBattlefield(player1, new PeaceOfMind());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new HonorGuard()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    @DisplayName("Discarding a card gains 3 life on resolution")
    void gains3LifeOnResolution() {
        harness.addToBattlefield(player1, new PeaceOfMind());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new HonorGuard()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        // Discard was paid as a cost
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Honor Guard");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities(); // resolve the ability

        harness.assertLife(player1, lifeBefore + 3);
    }

    @Test
    @DisplayName("Discards the selected card when several cards are in hand")
    void discardsSelectedCardFromMultipleCardHand() {
        harness.addToBattlefield(player1, new PeaceOfMind());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new HonorGuard(), new PeaceOfMind()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Peace of Mind");
        harness.assertInHand(player1, "Honor Guard");
    }

    @Test
    @DisplayName("Can activate repeatedly by paying each cost separately")
    void canActivateRepeatedly() {
        harness.addToBattlefield(player1, new PeaceOfMind());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of(new HonorGuard(), new PeaceOfMind()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertInGraveyard(player1, "Peace of Mind");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 3);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 6);
        harness.assertOnBattlefield(player1, "Peace of Mind");
    }

    @Test
    @DisplayName("Only the activating controller gains life")
    void playerTwoGainsLifeWhenActivating() {
        harness.addToBattlefield(player2, new PeaceOfMind());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new HonorGuard()));
        int playerOneLife = gd.getLife(player1.getId());
        int playerTwoLife = gd.getLife(player2.getId());

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Honor Guard");
        harness.assertLife(player2, playerTwoLife);

        harness.passBothPriorities();

        harness.assertLife(player1, playerOneLife);
        harness.assertLife(player2, playerTwoLife + 3);
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
        harness.setHand(player1, List.of(new HonorGuard()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
