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

@CardUsed({ExcaliburII.class, AngelOfMercy.class, GrizzlyBears.class})
class ExcaliburIITest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you gain life, Excalibur II gets a charge counter")
    void gainingLifeAddsChargeCounter() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburII());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(excalibur.getCounterCount(CounterType.CHARGE)).isZero();
    }
}
