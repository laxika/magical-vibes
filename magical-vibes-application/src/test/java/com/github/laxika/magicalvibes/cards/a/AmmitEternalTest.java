package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DefiantKhenra;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmmitEternal.class, DefiantKhenra.class, Abrade.class})
class AmmitEternalTest extends BaseCardTest {

    private Permanent getAmmit() {
        return findPermanent(player1, "Ammit Eternal");
    }

    private long triggersOnStack() {
        return gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
    }

    @Test
    @DisplayName("Opponent casting a spell mandatorily puts a -1/-1 counter on Ammit Eternal")
    void opponentSpellAddsMinusCounter() {
        harness.addToBattlefield(player1, new AmmitEternal());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DefiantKhenra()));
        harness.addMana(player2, ManaColor.RED, 2);

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

        harness.setHand(player2, List.of(new DefiantKhenra(), new DefiantKhenra()));
        harness.addMana(player2, ManaColor.RED, 4);

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
        harness.setHand(player1, List.of(new DefiantKhenra()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent ammit = getAmmit();

        harness.castCreature(player1, 0);

        assertThat(triggersOnStack()).isZero();
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dealing combat damage to a player removes all -1/-1 counters")
    void combatDamageRemovesCounters() {
        Permanent ammit = harness.addToBattlefieldAndReturn(player1, new AmmitEternal());
        ammit.setSummoningSick(false);
        ammit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        ammit.setAttacking(true);
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

    @Test
    @DisplayName("Afflict 3: becoming blocked makes the defending player lose 3 life")
    void blockedAfflictsDefender() {
        Permanent atk = harness.addToBattlefieldAndReturn(player1, new AmmitEternal());
        atk.setSummoningSick(false);
        atk.setAttacking(true);

        harness.addToBattlefield(player2, new DefiantKhenra());

        harness.setHand(player1, List.of());
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

    @Test
    @DisplayName("Afflict triggers only once when multiple creatures block")
    void multipleBlockersAfflictOnce() {
        Permanent ammit = harness.addToBattlefieldAndReturn(player1, new AmmitEternal());
        ammit.setSummoningSick(false);
        ammit.setAttacking(true);
        harness.addToBattlefield(player2, new DefiantKhenra());
        harness.addToBattlefield(player2, new DefiantKhenra());
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(triggersOnStack()).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Combat damage to a creature does not remove counters")
    void creatureCombatDamageKeepsCounters() {
        Permanent ammit = harness.addToBattlefieldAndReturn(player1, new AmmitEternal());
        ammit.setSummoningSick(false);
        ammit.setAttacking(true);
        ammit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addToBattlefield(player2, new DefiantKhenra());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.resolveCombatDamage();

        assertThat(triggersOnStack()).isZero();
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ammit Eternal");
        harness.assertInGraveyard(player2, "Defiant Khenra");
    }

    @Test
    @DisplayName("Combat trigger removes only minus counters and only from its source")
    void combatTriggerOnlyCleansItsSourceMinusCounters() {
        Permanent ammit = harness.addToBattlefieldAndReturn(player1, new AmmitEternal());
        ammit.setSummoningSick(false);
        ammit.setAttacking(true);
        ammit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        ammit.setCounterCount(CounterType.CHARGE, 1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AmmitEternal());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.resolveCombatDamage();

        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(triggersOnStack()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(ammit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The fifth minus counter kills Ammit before the opponent's spell resolves")
    void fifthCounterKillsBeforeSpellResolves() {
        Permanent ammit = harness.addToBattlefieldAndReturn(player1, new AmmitEternal());
        ammit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DefiantKhenra()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ammit Eternal");
        harness.assertNotOnBattlefield(player1, "Ammit Eternal");
        harness.assertNotOnBattlefield(player2, "Defiant Khenra");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Defiant Khenra");
    }

    @Test
    @DisplayName("An opponent's instant adds its counter before dealing damage")
    void opponentInstantAddsCounterBeforeResolving() {
        Permanent ammit = harness.addToBattlefieldAndReturn(player1, new AmmitEternal());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, 0, ammit.getId());

        assertThat(triggersOnStack()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ammit.getMarkedDamage()).isZero();
        harness.passBothPriorities();
        assertThat(ammit.getMarkedDamage()).isEqualTo(3);
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ammit Eternal");
    }
}
