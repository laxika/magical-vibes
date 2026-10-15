package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HagraConstrictor.class, CliffhavenSellSword.class})
class HagraConstrictorTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCountersAndHasMenace() {
        harness.castFromHand(player1, new HagraConstrictor(), "{2}{B}");
        harness.passBothPriorities();

        Permanent constrictor = findPermanent(player1, "Hagra Constrictor");
        assertThat(constrictor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, constrictor, Keyword.MENACE)).isTrue();
    }

    @Test
    void grantsMenaceOnlyToControlledCreaturesWithPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new HagraConstrictor());
        Permanent counteredOwnCreature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        Permanent uncounteredOwnCreature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        Permanent counteredOpponentCreature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        counteredOwnCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        counteredOpponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, counteredOwnCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredOwnCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, counteredOpponentCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void losesGrantedMenaceWhenItsPlusOnePlusOneCountersAreRemoved() {
        Permanent constrictor = harness.enterBattlefieldAndReturn(player1, new HagraConstrictor());

        assertThat(constrictor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, constrictor, Keyword.MENACE)).isTrue();

        constrictor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, constrictor, Keyword.MENACE)).isFalse();
    }

    @Test
    void menaceTracksCountersAddedAndRemovedAfterEntry() {
        harness.enterBattlefieldAndReturn(player1, new HagraConstrictor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    void otherCounterTypesDoNotGrantMenace() {
        harness.enterBattlefieldAndReturn(player1, new HagraConstrictor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        creature.setCounterCount(CounterType.CHARGE, 2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    void creaturesLoseMenaceWhenConstrictorDies() {
        Permanent constrictor = harness.enterBattlefieldAndReturn(player1, new HagraConstrictor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        constrictor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Hagra Constrictor");
        harness.assertInGraveyard(player1, "Hagra Constrictor");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    void grantedMenaceRejectsOneBlocker() {
        harness.enterBattlefieldAndReturn(player1, new HagraConstrictor());
        Permanent attacker = addCreatureReady(player1, new CliffhavenSellSword());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new CliffhavenSellSword());
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void grantedMenaceAllowsTwoBlockers() {
        harness.enterBattlefieldAndReturn(player1, new HagraConstrictor());
        Permanent attacker = addCreatureReady(player1, new CliffhavenSellSword());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent firstBlocker = addCreatureReady(player2, new CliffhavenSellSword());
        Permanent secondBlocker = addCreatureReady(player2, new CliffhavenSellSword());
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
