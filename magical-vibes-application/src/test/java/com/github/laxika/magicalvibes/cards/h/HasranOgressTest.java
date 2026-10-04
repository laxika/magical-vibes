package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HasranOgress.class})
class HasranOgressTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking without paying deals 3 damage to its controller")
    void attackingWithoutPayingDealsDamageToController() {
        addCreatureReady(player1, new HasranOgress());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(java.util.List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Paying {2} prevents the attack trigger damage")
    void payingManaPreventsDamage() {
        addCreatureReady(player1, new HasranOgress());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(java.util.List.of(0));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Controller may decline payment even with enough mana")
    void mayDeclineAffordablePayment() {
        addCreatureReady(player1, new HasranOgress());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(java.util.List.of(0));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Colored mana can pay the generic attack trigger cost")
    void coloredManaPaysGenericCost() {
        addCreatureReady(player1, new HasranOgress());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(java.util.List.of(0));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("An opponent's Ogress offers payment to and damages that opponent")
    void opponentsOgressDamagesItsController() {
        addCreatureReady(player2, new HasranOgress());
        int controllerLife = gd.getLife(player2.getId());
        int defenderLife = gd.getLife(player1.getId());

        declareAttackers(player2, java.util.List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(controllerLife - 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(defenderLife);
    }
}
