package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorozdaMonitor.class, DrudgeBeetle.class, Mountain.class})
class KorozdaMonitorTest extends BaseCardTest {

    private void readyScavenge() {
        harness.setGraveyard(player1, List.of(new KorozdaMonitor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Scavenge puts +1/+1 counters equal to Korozda Monitor's power (3) on target creature")
    void scavengePutsCountersEqualToPower() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Scavenge exiles Korozda Monitor as a cost, so it leaves the graveyard")
    void scavengeExilesTheCard() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Korozda Monitor");
    }

    @Test
    @DisplayName("Scavenge can target an opponent's creature")
    void scavengeCanTargetOpponentCreature() {
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Scavenge requires a creature target")
    void scavengeRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyScavenge();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scavenge can only be activated as a sorcery")
    void scavengeIsSorcerySpeedOnly() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new KorozdaMonitor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scavengeExilesSourceBeforeCountersArePlaced() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KorozdaMonitor());
        readyScavenge();
        var source = gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Korozda Monitor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
    }

    @Test
    void scavengeRequiresGreenMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KorozdaMonitor());
        harness.setGraveyard(player1, List.of(new KorozdaMonitor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Korozda Monitor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeRequiresSevenManaTotal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KorozdaMonitor());
        harness.setGraveyard(player1, List.of(new KorozdaMonitor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Korozda Monitor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeCannotActivateDuringCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KorozdaMonitor());
        readyScavenge();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Korozda Monitor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeCannotActivateWithAnAbilityOnTheStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KorozdaMonitor());
        readyScavenge();
        harness.setGraveyard(player1, List.of(new KorozdaMonitor(), new KorozdaMonitor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void scavengeCanActivateInPostcombatMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KorozdaMonitor());
        readyScavenge();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KorozdaMonitor());
        Permanent blocker = addCreatureReady(player2, new DrudgeBeetle());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Drudge Beetle");
        harness.assertOnBattlefield(player1, "Korozda Monitor");
    }

    @Test
    void scavengeRequiresTwoGreenMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new KorozdaMonitor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Korozda Monitor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sourceStaysExiledWhenTargetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        readyScavenge();
        var source = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.activateGraveyardAbility(player1, 0, target.getId());
        target.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drudge Beetle");
        harness.assertNotInGraveyard(player1, "Korozda Monitor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

}
