package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CaseyJonesAsphaltHooligan.class)
class CaseyJonesAsphaltHooliganTest extends BaseCardTest {

    @Test
    void activationDoublesPowerAndLeavesToughnessAlone() {
        Permanent casey = addCreatureReady(player1, new CaseyJonesAsphaltHooligan());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, casey)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, casey)).isEqualTo(2);
    }

    @Test
    void opponentMayActivateTheAbility() {
        Permanent casey = addCreatureReady(player1, new CaseyJonesAsphaltHooligan());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, casey)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, casey)).isEqualTo(2);
    }

    @Test
    void powerDoublingWearsOffAtEndOfTurn() {
        Permanent casey = addCreatureReady(player1, new CaseyJonesAsphaltHooligan());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, casey)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, casey)).isEqualTo(2);
    }

    @Test
    void stackedActivationsEachDoublePowerAtResolution() {
        Permanent casey = addCreatureReady(player1, new CaseyJonesAsphaltHooligan());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, casey)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, casey)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, casey)).isEqualTo(2);
    }

    @Test
    void negativePowerIsDoubledRatherThanClampedToZero() {
        Permanent casey = addCreatureReady(player1, new CaseyJonesAsphaltHooligan());
        casey.setPowerModifier(-3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, casey)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, casey)).isEqualTo(2);
    }

    @Test
    void zeroPowerRemainsZero() {
        Permanent casey = addCreatureReady(player1, new CaseyJonesAsphaltHooligan());
        casey.setPowerModifier(-2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, casey)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, casey)).isEqualTo(2);
    }

    @Test
    void abilityCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent casey = harness.addToBattlefieldAndReturn(player1, new CaseyJonesAsphaltHooligan());
        casey.setSummoningSick(true);
        casey.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, casey)).isEqualTo(4);
        assertThat(casey.isTapped()).isTrue();
    }

    @Test
    void doubledPowerDealsDamageInBothCombatDamageSteps() {
        addCreatureReady(player1, new CaseyJonesAsphaltHooligan());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 12);
    }
}
