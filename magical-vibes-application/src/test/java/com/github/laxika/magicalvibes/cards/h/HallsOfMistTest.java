package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallsOfMist.class, BalduvianBears.class})
class HallsOfMistTest extends BaseCardTest {

    private void advanceToNextUpkeep(Player activePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }

    @Test
    @DisplayName("A creature that attacked during its controller's last turn can't attack")
    void attackerFromLastTurnCantAttack() {
        Permanent bear = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        bear.setAttacking(true);
        bear.clearCombatState();

        // player1 -> player2 -> player1: back on the bear's controller's turn.
        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.addToBattlefieldAndReturn(player2, new HallsOfMist());

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);
    }

    @Test
    @DisplayName("A creature that did not attack during its controller's last turn may still attack")
    void nonAttackerCanAttack() {
        addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);
        harness.addToBattlefieldAndReturn(player2, new HallsOfMist());

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("The restriction only looks one controller turn back, so it lifts after a turn spent not attacking")
    void restrictionLiftsAfterAnIdleTurn() {
        Permanent bear = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        bear.setAttacking(true);
        bear.clearCombatState();

        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);
        Permanent halls = harness.addToBattlefieldAndReturn(player2, new HallsOfMist());
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);

        // The bear sits out this turn, so on the next one it is a legal attacker again.
        gd.playerBattlefields.get(player2.getId()).remove(halls);
        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.playerBattlefields.get(player2.getId()).add(halls);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Without Halls of Mist on the battlefield a creature may attack on consecutive turns")
    void noRestrictionWithoutHallsOfMist() {
        Permanent bear = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        bear.setAttacking(true);
        bear.clearCombatState();

        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @CardUsed(ImprisonedInTheMoon.class)
    @DisplayName("A Halls of Mist with no abilities imposes no attack restriction")
    void losingAllAbilitiesOnHallsDisablesAttackRestriction() {
        Permanent bear = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        bear.setAttacking(true);
        bear.clearCombatState();

        advanceToNextUpkeep(player2);
        advanceToNextUpkeep(player1);
        Permanent halls = harness.addToBattlefieldAndReturn(player2, new HallsOfMist());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(halls.getId());

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Halls of Mist on the battlefield")
    void payingCumulativeUpkeepKeepsHallsOfMist() {
        Permanent halls = harness.addToBattlefieldAndReturn(player1, new HallsOfMist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(halls);
        assertThat(halls.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Halls of Mist")
    void decliningCumulativeUpkeepSacrificesHallsOfMist() {
        Permanent halls = harness.addToBattlefieldAndReturn(player1, new HallsOfMist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(halls);
        harness.assertInGraveyard(player1, "Halls of Mist");
    }
}
