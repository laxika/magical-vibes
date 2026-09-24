package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Megatherium.class)
class MegatheriumTest extends BaseCardTest {

    private void castMegatheriumWithTwoCardsLeftInHand() {
        harness.setHand(player1, List.of(new Megatherium(), new Megatherium(), new Megatherium()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void castMegatheriumWithNoCardsLeftInHand() {
        harness.setHand(player1, List.of(new Megatherium()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("Paying {1} for each card in hand keeps Megatherium on the battlefield")
    void payingForCardsInHandKeepsMegatherium() {
        castMegatheriumWithTwoCardsLeftInHand();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Megatherium");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Megatherium")
    void acceptingWithoutEnoughManaSacrificesMegatherium() {
        castMegatheriumWithTwoCardsLeftInHand();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Megatherium");
        harness.assertInGraveyard(player1, "Megatherium");
    }

    @Test
    @DisplayName("With no cards left in hand, paying zero keeps Megatherium on the battlefield")
    void noCardsLeftInHandRequireNoAdditionalPayment() {
        castMegatheriumWithNoCardsLeftInHand();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Megatherium");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining to pay sacrifices Megatherium")
    void decliningPaymentSacrificesMegatherium() {
        castMegatheriumWithTwoCardsLeftInHand();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Megatherium");
        harness.assertInGraveyard(player1, "Megatherium");
    }
}
