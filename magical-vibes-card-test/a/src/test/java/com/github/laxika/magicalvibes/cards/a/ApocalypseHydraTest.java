package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EmberWeaver;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApocalypseHydra.class, EmberWeaver.class})
class ApocalypseHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters (below the doubling threshold)")
    void entersWithXCountersBelowThreshold() {
        harness.setHand(player1, List.of(new ApocalypseHydra()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // 3 generic for X

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=4 enters with 4 +1/+1 counters (still below the doubling threshold)")
    void entersWithXCountersJustBelowThreshold() {
        harness.setHand(player1, List.of(new ApocalypseHydra()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 4, null, null);
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting with X=5 enters with an additional X counters (10 total)")
    void entersWithDoubledCountersAtThreshold() {
        harness.setHand(player1, List.of(new ApocalypseHydra()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 5, null, null);
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    @Test
    @DisplayName("Ability deals 1 damage to a creature and removes a +1/+1 counter as cost")
    void abilityDealsDamageToCreatureAndRemovesCounter() {
        Permanent hydra = addReadyHydra(player1, 4);
        harness.addToBattlefield(player2, new EmberWeaver());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Ember Weaver");
        harness.activateAbility(player1, 0, null, targetId);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player2, "Ember Weaver");
        assertThat(weaver.getMarkedDamage()).isEqualTo(1);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability deals 1 damage to a player")
    void abilityDealsDamageToPlayer() {
        addReadyHydra(player1, 2);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot activate the ability with no +1/+1 counters to remove")
    void cannotActivateWithoutCounters() {
        addReadyHydra(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the ability without paying {1}{R}")
    void cannotActivateWithoutMana() {
        addReadyHydra(player1, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting with X=0 leaves no counters and the Hydra dies")
    void zeroXDies() {
        harness.setHand(player1, List.of(new ApocalypseHydra()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Apocalypse Hydra");
        harness.assertInGraveyard(player1, "Apocalypse Hydra");
    }

    @Test
    @DisplayName("Casting with X=6 enters with twelve counters")
    void doublesAboveThreshold() {
        harness.setHand(player1, List.of(new ApocalypseHydra()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 6, null, null);
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast gives zero counters")
    void enteringWithoutCastingDies() {
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new ApocalypseHydra());

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Apocalypse Hydra");
        harness.assertInGraveyard(player1, "Apocalypse Hydra");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Hydra can activate more than once")
    void tappedSummoningSickHydraCanActivateRepeatedly() {
        Permanent hydra = addReadyHydra(player1, 3);
        hydra.setSummoningSick(true);
        hydra.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hydra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the last counter kills the Hydra but its ability still deals damage")
    void lastCounterAbilityResolvesAfterSourceDies() {
        addReadyHydra(player1, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Apocalypse Hydra");
        harness.assertInGraveyard(player1, "Apocalypse Hydra");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @CardUsed({HardenedScales.class})
    @DisplayName("Hardened Scales adds only one counter to the full entry total")
    void scalesAppliesOnceToDoubledEntryCounters() {
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new ApocalypseHydra()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 5, null, null);
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(11);
    }

    private Permanent addReadyHydra(Player player, int counters) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ApocalypseHydra());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return perm;
    }

    private Permanent findHydra(Player player) {
        return findPermanent(player, "Apocalypse Hydra");
    }
}
