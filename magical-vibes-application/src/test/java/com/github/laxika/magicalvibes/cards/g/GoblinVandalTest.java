package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinVandalTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent atk = new Permanent(new GoblinVandal());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atk);
        return atk;
    }

    private Permanent addDefenderArtifact() {
        Permanent millstone = new Permanent(new Millstone());
        gd.playerBattlefields.get(player2.getId()).add(millstone);
        return millstone;
    }

    private void advanceToMayPayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        // Defender declares no blocks, so Goblin Vandal is unblocked and its trigger fires.
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying {R} destroys the chosen artifact and prevents the Vandal's combat damage")
    void payDestroysArtifactAndPreventsDamage() {
        Permanent millstone = addDefenderArtifact();
        Permanent attacker = addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToMayPayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(millstone.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(millstone);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Declining to pay leaves the artifact alone and the Vandal deals combat damage")
    void declineDoesNothing() {
        Permanent millstone = addDefenderArtifact();
        Permanent attacker = addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToMayPayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(millstone);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Only artifacts the defending player controls can be destroyed")
    void nonArtifactsAreNotOffered() {
        // The defending player controls a land but no artifact.
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new Forest()));
        Permanent attacker = addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToMayPayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("A blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
