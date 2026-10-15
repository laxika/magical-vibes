package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RimeshieldFrostGiant.class, GiantGrowth.class, Shock.class, RodOfRuin.class})
class RimeshieldFrostGiantTest extends BaseCardTest {

    @Test
    void wardCountersOpponentSpellWhenTheyDoNotPay() {
        Permanent giant = addReadyGiant();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardAllowsOpponentSpellWhenTheyPayThreeMana() {
        Permanent giant = addReadyGiant();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player2, 0, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(8);
    }

    @Test
    void wardCountersSpellWhenOpponentDeclinesAffordablePayment() {
        Permanent giant = addReadyGiant();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player2, 0, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(5);
    }

    @Test
    void wardDoesNotTaxControllersOwnSpell() {
        Permanent giant = addReadyGiant();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardCountersOpponentActivatedAbilityWithoutPayment() {
        Permanent giant = addReadyGiant();
        harness.addToBattlefield(player2, new RodOfRuin());
        prepareOpponentTurn();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Rod of Ruin");
    }

    private Permanent addReadyGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new RimeshieldFrostGiant());
        giant.setSummoningSick(false);
        return giant;
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
