package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.InvasionOfKamigawa;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RooftopSaboteurs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PortentTracker.class, InvasionOfKamigawa.class, Mountain.class, RooftopSaboteurs.class})
class PortentTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability untaps target land")
    void untapsTargetLand() {
        Permanent tracker = addTracker();
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();

        harness.activateAbility(player1, indexOf(player1, tracker), 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap ability cannot target a nonland permanent")
    void cannotUntapNonlandPermanent() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player1, player2.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, tracker), 0, null, battle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removes a defense counter from a battle an opponent protects")
    void removesDefenseCounterWhenOpponentProtectsBattle() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player1, player2.getId(), 3);

        harness.activateAbility(player1, indexOf(player1, tracker), 1, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts a defense counter on a battle its controller protects")
    void putsDefenseCounterOnBattleProtectedByController() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player2, player1.getId(), 1);

        harness.activateAbility(player1, indexOf(player1, tracker), 1, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can untap an opponent's land and taps the tracker as the cost")
    void untapsOpponentsLandAndPaysTapCostImmediately() {
        Permanent tracker = addTracker();
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.tap();

        harness.activateAbility(player1, indexOf(player1, tracker), 0, null, mountain.getId());

        assertThat(tracker.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The battle ability cannot target a land")
    void battleAbilityCannotTargetLand() {
        Permanent tracker = addTracker();
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, tracker), 1, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The battle ability is sorcery speed: not in an upkeep")
    void battleAbilityCannotBeActivatedOutsideMainPhase() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player1, player2.getId(), 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, tracker), 1, null, battle.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The battle ability is sorcery speed: not on an opponent's turn")
    void battleAbilityCannotBeActivatedOnOpponentsTurn() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player1, player2.getId(), 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, tracker), 1, null, battle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The battle ability is sorcery speed: not with a nonempty stack")
    void battleAbilityCannotBeActivatedWithNonemptyStack() {
        Permanent firstTracker = addTracker();
        Permanent secondTracker = addTracker();
        Permanent battle = addBattle(player1, player2.getId(), 3);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();
        harness.activateAbility(player1, indexOf(player1, firstTracker), 0, null, mountain.getId());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, secondTracker), 1, null, battle.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The land ability can be activated outside a main phase")
    void landAbilityCanBeActivatedOutsideMainPhase() {
        Permanent tracker = addTracker();
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, indexOf(player1, tracker), 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick tracker can activate neither tap ability")
    void summoningSickTrackerCannotActivateEitherTapAbility() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        tracker.setSummoningSick(true);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent battle = addBattle(player1, player2.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, tracker), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, tracker), 1, null, battle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The battle ability reads the protector as it resolves")
    void battleAbilityUsesProtectorAtResolution() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player1, player2.getId(), 3);
        harness.activateAbility(player1, indexOf(player1, tracker), 1, null, battle.getId());

        gd.playerBattlefields.get(player1.getId()).remove(battle);
        gd.playerBattlefields.get(player2.getId()).add(battle);
        battle.setProtectorPlayerId(player1.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing the last defense counter from a Siege an opponent protects defeats it")
    void removingTheLastDefenseCounterDefeatsTheSiege() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player1, player2.getId(), 1);

        harness.activateAbility(player1, indexOf(player1, tracker), 1, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isZero();
        harness.assertOnBattlefield(player1, "Invasion of Kamigawa");
        harness.assertNotInGraveyard(player1, "Invasion of Kamigawa");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rooftop Saboteurs");
        harness.assertNotOnBattlefield(player1, "Invasion of Kamigawa");
    }

    private Permanent addBattle(com.github.laxika.magicalvibes.model.Player controller,
                                java.util.UUID protectorId, int defenseCounters) {
        Permanent battle = harness.addToBattlefieldAndReturn(controller, new InvasionOfKamigawa());
        battle.setProtectorPlayerId(protectorId);
        battle.setCounterCount(CounterType.DEFENSE, defenseCounters);
        return battle;
    }

    private Permanent addTracker() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        tracker.setSummoningSick(false);
        return tracker;
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
