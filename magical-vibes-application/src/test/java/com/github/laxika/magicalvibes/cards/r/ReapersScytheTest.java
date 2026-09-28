package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReapersScythe.class, GrizzlyBears.class})
class ReapersScytheTest extends BaseCardTest {

    @Test
    void addsOneSoulCounterForEachPlayerWhoLostLifeThisTurn() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        gd.lifeLostThisTurn.put(player1.getId(), 3);
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(2);
    }

    @Test
    void soulCountersBoostAndMakeTheEquippedCreatureAnAssassin() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        scythe.setAttachedTo(creature.getId());
        scythe.setCounterCount(CounterType.SOUL, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.ASSASSIN)).isTrue();
    }

    @Test
    void equipTwoAttachesTheScytheToAcreatureYouControl() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
    }
}
