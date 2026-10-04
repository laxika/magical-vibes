package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExcaliburII.class, AngelOfMercy.class, GrizzlyBears.class})
class ExcaliburIITest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you gain life, Excalibur II gets a charge counter")
    void gainingLifeAddsChargeCounter() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(excalibur.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each charge counter on Excalibur II")
    void equippedCreatureGetsBoostForEachChargeCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        excalibur.setAttachedTo(creature.getId());
        excalibur.setCounterCount(CounterType.CHARGE, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Excalibur II does not trigger when an opponent gains life")
    void opponentGainingLifeDoesNotAddChargeCounter() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(excalibur.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void separateLifeGainEventsEachAddOneCounterAndUpdateEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        excalibur.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new AngelOfMercy(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(excalibur.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 26);
        assertThat(excalibur.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void equipForThreeManaMovesBonusToNewCreatureAndKeepsCounters() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        excalibur.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();

        assertThat(excalibur.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(excalibur.getAttachedTo()).isEqualTo(second.getId());
        assertThat(excalibur.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    void bonusCountsOnlyChargeCountersOnEquipmentAndShrinksWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        excalibur.setAttachedTo(creature.getId());
        creature.setCounterCount(CounterType.CHARGE, 5);
        excalibur.setCounterCount(CounterType.CHARGE, 2);
        excalibur.setCounterCount(CounterType.QUEST, 3);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        excalibur.setCounterCount(CounterType.CHARGE, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void equipRejectsOpponentsCreature() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(excalibur.getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresThreeMana() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(excalibur.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(excalibur.getAttachedTo()).isNull();
    }
}
