package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@CardUsed({DamningVerdict.class, GrizzlyBears.class, HowlingMine.class})
class DamningVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures without counters and leaves countered creatures and noncreatures")
    void destroysOnlyCreaturesWithoutCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.CHARGE, 1);
        harness.addToBattlefield(player1, new HowlingMine());

        castDamningVerdict();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player1, "Damning Verdict");
    }

    @Test
    @DisplayName("Destroys creatures without counters controlled by either player")
    void destroysCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDamningVerdict();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @ParameterizedTest
    @EnumSource(value = CounterType.class, names = {"PLUS_ONE_PLUS_ONE", "MINUS_ONE_MINUS_ONE", "VIGILANCE", "SHIELD"})
    @DisplayName("Any kind of counter protects a creature from the verdict")
    void sparesCreaturesWithDifferentCounterTypes(CounterType counterType) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(counterType, 1);

        castDamningVerdict();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(creature.getCounterCount(counterType)).isEqualTo(1);
    }

    @Test
    @DisplayName("A counter added before resolution protects the creature")
    void checksCountersAddedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new DamningVerdict(), "{3}{W}{W}");
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature whose last counter was removed before resolution is destroyed")
    void checksCountersRemovedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.castFromHand(player1, new DamningVerdict(), "{3}{W}{W}");
        creature.setCounterCount(CounterType.CHARGE, 0);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves with no creatures and leaves noncreatures intact")
    void resolvesWithoutCreatures() {
        harness.addToBattlefield(player2, new HowlingMine());

        castDamningVerdict();

        harness.assertOnBattlefield(player2, "Howling Mine");
        harness.assertInGraveyard(player1, "Damning Verdict");
    }

    private void castDamningVerdict() {
        harness.castFromHand(player1, new DamningVerdict(), "{3}{W}{W}");
        harness.passBothPriorities();
    }
}
