package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmmitEternalTest extends BaseCardTest {

    private Permanent getAmmit() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ammit Eternal"))
                .findFirst()
                .orElseThrow();
    }

    private long triggersOnStack() {
        return gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
    }

    // ===== Whenever an opponent casts a spell, put a -1/-1 counter on this creature =====

    @Test
    @DisplayName("Opponent casting a spell mandatorily puts a -1/-1 counter on Ammit Eternal")
    void opponentSpellAddsMinusCounter() {
        harness.addToBattlefield(player1, new AmmitEternal());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        Permanent ammit = getAmmit();
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.castCreature(player2, 0);

        // Mandatory trigger — goes straight on the stack, no "may" prompt.
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(triggersOnStack()).isEqualTo(1);

        harness.passBothPriorities(); // resolve the trigger
        harness.passBothPriorities(); // resolve the opponent's spell

        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ammit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ammit)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each opponent spell adds another -1/-1 counter")
    void opponentSpellsStackCounters() {
        harness.addToBattlefield(player1, new AmmitEternal());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        Permanent ammit = getAmmit();

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // trigger
        harness.passBothPriorities(); // spell
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // trigger
        harness.passBothPriorities(); // spell
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller casting a spell does not trigger Ammit Eternal")
    void controllerSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AmmitEternal());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent ammit = getAmmit();

        harness.castCreature(player1, 0);

        assertThat(triggersOnStack()).isZero();
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    // ===== Whenever this creature deals combat damage to a player, remove all -1/-1 counters =====

    @Test
    @DisplayName("Dealing combat damage to a player removes all -1/-1 counters")
    void combatDamageRemovesCounters() {
        Permanent ammit = new Permanent(new AmmitEternal());
        ammit.setSummoningSick(false);
        ammit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        ammit.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(ammit);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // 5 base power - 2 counters = 3 unblocked combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities(); // resolve the remove-counters trigger

        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, ammit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ammit)).isEqualTo(5);
    }

    // ===== Afflict 3 =====

    @Test
    @DisplayName("Afflict 3: becoming blocked makes the defending player lose 3 life")
    void blockedAfflictsDefender() {
        Permanent atk = new Permanent(new AmmitEternal());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atk);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Afflict is not a drain: the defender loses 3, the attacking player's life is unchanged.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
