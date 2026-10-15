package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Afflict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
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

@CardUsed({ClingingAnemones.class, GrizzlyBears.class, GiantGrowth.class, Shock.class,
        WallOfAir.class, Afflict.class})
class ClingingAnemonesTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Clinging Anemones when a larger creature enters")
    void evolvesForLargerCreature() {
        Permanent anemones = harness.addToBattlefieldAndReturn(player1, new ClingingAnemones());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(anemones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger when neither stat is greater")
    void doesNotEvolveForEqualCreature() {
        Permanent anemones = harness.addToBattlefieldAndReturn(player1, new ClingingAnemones());

        harness.setHand(player1, List.of(new ClingingAnemones()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(anemones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve uses the entering creature's last-known stats if it leaves before resolution")
    void evolvesAfterEnteringCreatureLeaves() {
        Permanent anemones = harness.addToBattlefieldAndReturn(player1, new ClingingAnemones());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(anemones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve rechecks the comparison when its trigger resolves")
    void rechecksComparisonAtResolution() {
        Permanent anemones = harness.addToBattlefieldAndReturn(player1, new ClingingAnemones());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, anemones.getId());
        harness.passBothPriorities();

        assertThat(anemones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Defender prevents Clinging Anemones from attacking")
    void cannotAttack() {
        addCreatureReady(player1, new ClingingAnemones());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Defender does not prevent Clinging Anemones from blocking")
    void canBlock() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent anemones = addCreatureReady(player2, new ClingingAnemones());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(anemones.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Evolve triggers when only the entering creature's toughness is greater")
    void evolvesForGreaterToughnessOnly() {
        Permanent anemones = harness.addToBattlefieldAndReturn(player1, new ClingingAnemones());

        harness.setHand(player1, List.of(new WallOfAir()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(anemones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's larger creature does not trigger evolve")
    void doesNotEvolveForOpponentCreature() {
        Permanent anemones = harness.addToBattlefieldAndReturn(player1, new ClingingAnemones());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(anemones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Resolving one evolve trigger can make a second trigger fail its comparison")
    void earlierCounterPreventsSecondEvolution() {
        Permanent anemones = harness.addToBattlefieldAndReturn(player1, new ClingingAnemones());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(anemones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve uses the entering creature's changed stats immediately before it leaves")
    void usesChangedLastKnownStats() {
        Permanent anemones = harness.addToBattlefieldAndReturn(player1, new ClingingAnemones());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setLibrary(player1, List.of(new ClingingAnemones()));
        harness.setHand(player1, List.of(new Afflict(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(anemones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
