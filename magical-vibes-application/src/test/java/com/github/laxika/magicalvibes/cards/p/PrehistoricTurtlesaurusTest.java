package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
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

@CardUsed({PrehistoricTurtlesaurus.class, GrizzlyBears.class, SolRing.class})
class PrehistoricTurtlesaurusTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast for only four mana without a creature with a +1/+1 counter")
    void cannotCastWithoutCounteredCreature() {
        harness.setHand(player1, List.of(new PrehistoricTurtlesaurus()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs one less to cast while controlling a creature with a +1/+1 counter")
    void costsOneLessWithCounteredCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new PrehistoricTurtlesaurus()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void castsAtFullCostWithoutCounters() {
        harness.setHand(player1, List.of(new PrehistoricTurtlesaurus()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prehistoric Turtlesaurus");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void opponentsCounteredCreatureDoesNotReduceCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PrehistoricTurtlesaurus());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new PrehistoricTurtlesaurus()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void otherCounterTypesDoNotReduceCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrehistoricTurtlesaurus());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new PrehistoricTurtlesaurus()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void counteredNoncreatureDoesNotReduceCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        artifact.tap();
        harness.setHand(player1, List.of(new PrehistoricTurtlesaurus()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleCreaturesAndCountersStillReduceCostByOnlyOne() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PrehistoricTurtlesaurus());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PrehistoricTurtlesaurus());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new PrehistoricTurtlesaurus()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionDoesNotPayTheGreenRequirement() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrehistoricTurtlesaurus());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new PrehistoricTurtlesaurus()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void attackingDoesNotTapTurtlesaurus() {
        Permanent creature = addCreatureReady(player1, new PrehistoricTurtlesaurus());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }
}
