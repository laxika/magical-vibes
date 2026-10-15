package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinwuWhiteMage.class, MasterApothecary.class, GrizzlyBears.class, AngelOfMercy.class,
        ArtificialEvolution.class, Bitterblossom.class})
class MinwuWhiteMageTest extends BaseCardTest {

    @Test
    @DisplayName("Gaining life puts a +1/+1 counter on each Cleric you control")
    void gainingLifeCountersControlledClerics() {
        Permanent minwu = harness.addToBattlefieldAndReturn(player1, new MinwuWhiteMage());
        Permanent ownCleric = harness.addToBattlefieldAndReturn(player1, new MasterApothecary());
        Permanent nonCleric = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCleric = harness.addToBattlefieldAndReturn(player2, new MasterApothecary());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Angel of Mercy resolves.
        harness.passBothPriorities(); // Angel of Mercy's life-gain ability resolves.
        harness.passBothPriorities(); // Minwu's triggered ability resolves.

        assertThat(minwu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonCleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void lifelinkCombatDamageTriggersCounters() {
        Permanent minwu = addCreatureReady(player1, new MinwuWhiteMage());
        minwu.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(minwu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(minwu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentLifeGainDoesNotTriggerMinwu() {
        Permanent minwu = harness.addToBattlefieldAndReturn(player1, new MinwuWhiteMage());

        harness.enterBattlefieldAndReturn(player2, new AngelOfMercy());
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        assertThat(minwu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void separateLifeGainEventsEachPutOneCounter() {
        Permanent minwu = harness.addToBattlefieldAndReturn(player1, new MinwuWhiteMage());

        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 26);
        assertThat(minwu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void clericEnteringAfterLifeGainReceivesCounterAtResolution() {
        Permanent minwu = harness.addToBattlefieldAndReturn(player1, new MinwuWhiteMage());
        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();

        Permanent cleric = harness.enterBattlefieldAndReturn(player1, new MasterApothecary());
        harness.passBothPriorities();

        assertThat(minwu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void noncreatureKindredClericReceivesCounter() {
        Permanent minwu = harness.addToBattlefieldAndReturn(player1, new MinwuWhiteMage());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, enchantment.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "CLERIC");
        assertThat(gqs.hasEffectiveSubtype(gd, enchantment, CardSubtype.CLERIC)).isTrue();
        assertThat(gqs.isCreature(gd, enchantment)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(minwu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enchantment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
