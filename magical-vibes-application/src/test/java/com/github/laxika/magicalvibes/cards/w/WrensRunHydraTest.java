package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WrensRunHydra.class, Forest.class, GrizzlyBears.class, OrnithopterOfParadise.class})
class WrensRunHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new WrensRunHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3);

        Permanent hydra = findPermanent(player1, "Wren's Run Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hydra.getEffectivePower()).isEqualTo(3);
        assertThat(hydra.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Reinforce X puts X +1/+1 counters on target creature")
    void reinforcePutsXCounters() {
        harness.setHand(player1, List.of(new WrensRunHydra()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, bears.getId(), 3);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Wren's Run Hydra");
    }

    @Test
    @DisplayName("Reinforce requires a creature target")
    void reinforceRejectsNonCreature() {
        harness.setHand(player1, List.of(new WrensRunHydra()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, forest.getId(), 3))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Wren's Run Hydra");
    }

    @Test
    @DisplayName("Casting with X zero puts the Hydra into the graveyard")
    void castingWithZeroXDies() {
        harness.setHand(player1, List.of(new WrensRunHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Wren's Run Hydra");
        harness.assertInGraveyard(player1, "Wren's Run Hydra");
    }

    @Test
    @DisplayName("Reinforce can target an opponent's creature and discards as a cost")
    void reinforceTargetsOpponentAndDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new WrensRunHydra()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, bears.getId(), 2);

        harness.assertNotInHand(player1, "Wren's Run Hydra");
        harness.assertInGraveyard(player1, "Wren's Run Hydra");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Reinforce with X zero still costs two green mana and discards the card")
    void reinforceWithZeroX() {
        harness.setHand(player1, List.of(new WrensRunHydra()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, bears.getId(), 0);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInHand(player1, "Wren's Run Hydra");
        harness.assertInGraveyard(player1, "Wren's Run Hydra");
    }

    @Test
    @DisplayName("Reinforce rejects insufficient mana without discarding")
    void reinforceRequiresManaForXAndBothGreenSymbols() {
        harness.setHand(player1, List.of(new WrensRunHydra()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId(), 3))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Wren's Run Hydra");
        harness.assertNotInGraveyard(player1, "Wren's Run Hydra");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Reach allows the Hydra to block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new OrnithopterOfParadise());
        Permanent hydra = addCreatureReady(player2, new WrensRunHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(hydra.isBlocking()).isTrue();
    }
}
