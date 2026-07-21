package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NehebTheEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Afflict 3: becoming blocked makes the defending player lose 3 life")
    void blockedAfflictsDefender() {
        Permanent atk = new Permanent(new NehebTheEternal());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
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

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Postcombat main: adds {R} equal to life opponents lost this turn")
    void postcombatMainAddsRedEqualToOpponentLifeLost() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Clear leftover mana from casting so the pool is clean for the trigger.
        gd.playerManaPools.get(player1.getId()).clear();

        advanceToPostcombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    @DisplayName("Postcombat main: controller's own life loss does not produce mana")
    void ownLifeLossDoesNotCount() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        gd.playerManaPools.get(player1.getId()).clear();

        advanceToPostcombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger on precombat main")
    void doesNotTriggerOnPrecombatMain() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        gd.lifeLostThisTurn.put(player2.getId(), 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on an opponent's postcombat main")
    void doesNotTriggerOnOpponentsPostcombatMain() {
        harness.addToBattlefield(player1, new NehebTheEternal());
        gd.lifeLostThisTurn.put(player1.getId(), 5);

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    private void advanceToPostcombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
