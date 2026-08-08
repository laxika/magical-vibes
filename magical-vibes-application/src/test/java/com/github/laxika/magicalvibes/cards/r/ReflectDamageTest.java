package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectDamageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reflect Damage prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castReflectDamage(player1);
        addReadyGoblin(player2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot redirection shield")
    void choosingSourceRecordsShield() {
        castReflectDamage(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.reflectDamageToSourceControllerShields).contains(goblin.getId());
    }

    @Test
    @DisplayName("The chosen attacker's combat damage is dealt to its own controller instead")
    void redirectsCombatDamageToSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castReflectDamage(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.reflectDamageToSourceControllerShields).isEmpty();
    }

    @Test
    @DisplayName("A different source deals its damage normally and the shield is untouched")
    void differentSourceNotRedirected() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castReflectDamage(player1);
        Permanent chosen = addReadyGoblin(player2);
        Permanent other = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.reflectDamageToSourceControllerShields).contains(chosen.getId());
    }

    @Test
    @DisplayName("Only the next damage event is redirected")
    void onlyNextDamageEventIsRedirected() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castReflectDamage(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        goblin.setAttacking(true);
        resolveCombat(player2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castReflectDamage(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.reflectDamageToSourceControllerShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.reflectDamageToSourceControllerShields).isEmpty();
    }

    private void castReflectDamage(Player player) {
        harness.setHand(player, List.of(new ReflectDamage()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.castInstant(player, 0);
    }

    private Permanent addReadyGoblin(Player player) {
        Permanent perm = new Permanent(new GoblinPiker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
