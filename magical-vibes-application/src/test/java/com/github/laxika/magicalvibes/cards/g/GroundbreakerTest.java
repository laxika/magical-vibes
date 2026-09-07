package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Groundbreaker.class, GrizzlyBears.class})
class GroundbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent groundbreaker = new Permanent(new Groundbreaker());
        groundbreaker.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(groundbreaker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent groundbreaker = new Permanent(new Groundbreaker());
        groundbreaker.setSummoningSick(false);
        groundbreaker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(groundbreaker);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                bears.getId(), 2,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Groundbreaker");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices itself at end step")
    void sacrificesItselfAtEndStep() {
        Permanent groundbreaker = new Permanent(new Groundbreaker());
        gd.playerBattlefields.get(player1.getId()).add(groundbreaker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Groundbreaker");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(groundbreaker.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Groundbreaker");
        harness.assertInGraveyard(player1, "Groundbreaker");
    }

    @Test
    @DisplayName("Casting requires three green mana")
    void castingRequiresThreeGreenMana() {
        harness.setHand(player1, List.of(new Groundbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Groundbreaker");
    }
}
