package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BribersPurseTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with gem counters equal to the value of X")
    void entersWithXGemCounters() {
        harness.setHand(player1, List.of(new BribersPurse()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent purse = findPermanent(player1, "Briber's Purse");
        assertThat(purse.getCounterCount(CounterType.GEM)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a gem counter stops a creature from attacking or blocking this turn")
    void removesGemCounterAndLocksCreature() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(purse.getCounterCount(CounterType.GEM)).isZero();
        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("The creature lock expires at end of turn")
    void lockExpiresAtEndOfTurn() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareAttack(bears)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot activate without a gem counter")
    void cannotActivateWithoutGemCounter() {
        addReadyPurse(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent purse = addReadyPurse(player1);
        purse.setCounterCount(CounterType.GEM, 1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyPurse(Player player) {
        Permanent purse = new Permanent(new BribersPurse());
        purse.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(purse);
        return purse;
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        gs.declareAttackers(gd, player2, List.of(index));
    }
}
