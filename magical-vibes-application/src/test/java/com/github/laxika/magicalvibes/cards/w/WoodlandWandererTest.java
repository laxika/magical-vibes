package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodlandWanderer.class})
class WoodlandWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for one color of mana")
    void entersWithOneCounterForOneColor() {
        harness.setHand(player1, List.of(new WoodlandWanderer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Woodland Wanderer");
        assertThat(wanderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with one counter for each distinct color of mana")
    void entersWithCountersForDistinctColors() {
        harness.setHand(player1, List.of(new WoodlandWanderer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Woodland Wanderer");
        assertThat(wanderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
    @Test
    @DisplayName("Enters with four counters when four colors pay its mana cost")
    void entersWithFourCountersForFourColors() {
        harness.setHand(player1, List.of(new WoodlandWanderer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Woodland Wanderer");
        assertThat(wanderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Unspent mana does not contribute to converge")
    void countsOnlyColorsActuallySpent() {
        harness.setHand(player1, List.of(new WoodlandWanderer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Woodland Wanderer");
        assertThat(wanderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Entering without being cast gives no converge counters")
    void entersWithoutCountersWhenNotCast() {
        harness.addMana(player1, ManaColor.GREEN, 4);

        Permanent wanderer = harness.enterBattlefieldAndReturn(player1, new WoodlandWanderer());

        assertThat(wanderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacks without tapping and tramples excess damage over a blocker")
    void vigilanceAndTrampleWorkWithConvergeCounters() {
        harness.setHand(player1, List.of(new WoodlandWanderer()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent attacker = findPermanent(player1, "Woodland Wanderer");
        attacker.setSummoningSick(false);
        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new WoodlandWanderer());

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(attacker.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Woodland Wanderer");
        harness.assertOnBattlefield(player1, "Woodland Wanderer");
    }
}
