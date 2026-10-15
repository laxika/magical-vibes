package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GiantTortoise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HulklingBurgeoningBruiser.class, CentaurCourser.class, GiantTortoise.class,
        GrizzlyBears.class})
class HulklingBurgeoningBruiserTest extends BaseCardTest {

    @Test
    void putsCounterWhenEnteringCreatureHasGreaterPower() {
        Permanent hulkling = addHulkling();

        castCreature(new CentaurCourser(), "{2}{G}");

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterWhenEnteringCreatureHasGreaterToughness() {
        Permanent hulkling = addHulkling();

        castCreature(new GiantTortoise(), "{1}{U}");

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenNeitherCharacteristicIsGreater() {
        Permanent hulkling = addHulkling();

        castCreature(new GrizzlyBears(), "{1}{G}");

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void checksTheComparisonAgainWhenTheAbilityResolves() {
        Permanent hulkling = addHulkling();

        harness.castFromHand(player1, new CentaurCourser(), "{2}{G}");
        harness.passBothPriorities();

        hulkling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForAnOpponentsCreature() {
        Permanent hulkling = addHulkling();

        harness.enterBattlefieldAndReturn(player2, new CentaurCourser());

        assertThat(gd.stack).isEmpty();
        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenToughnessIsEqualAndPowerIsLower() {
        Permanent hulkling = addHulkling();
        hulkling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.enterBattlefieldAndReturn(player1, new GiantTortoise());

        assertThat(gd.stack).isEmpty();
        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.castFromHand(player1, new HulklingBurgeoningBruiser(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent hulkling = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multiplePendingTriggersEachRecheckTheComparison() {
        Permanent hulkling = addHulkling();
        harness.enterBattlefieldAndReturn(player1, new CentaurCourser());
        harness.enterBattlefieldAndReturn(player1, new CentaurCourser());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesTheEnteringCreaturesCurrentCharacteristicsAtResolution() {
        Permanent hulkling = addHulkling();
        Permanent tortoise = harness.enterBattlefieldAndReturn(player1, new GiantTortoise());
        assertThat(gd.stack).hasSize(1);

        tortoise.tap();
        harness.passBothPriorities();

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void usesCharacteristicsImmediatelyBeforeTheEnteringCreatureLeft() {
        Permanent hulkling = addHulkling();
        Permanent courser = harness.enterBattlefieldAndReturn(player1, new CentaurCourser());
        hulkling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        courser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, courser));
        harness.passBothPriorities();

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent addHulkling() {
        return harness.addToBattlefieldAndReturn(player1, new HulklingBurgeoningBruiser());
    }

    private void castCreature(Card creature, String manaCost) {
        harness.castFromHand(player1, creature, manaCost);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
