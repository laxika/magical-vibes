package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResoluteSurvivorsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadySurvivors(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting deals 1 damage to each opponent and gains 1 life")
    void exertDamagesOpponentAndGainsLife() {
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        Permanent survivors = addReadySurvivors(player1);
        int combatDamage = gqs.getEffectivePower(gd, survivors);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        // Combat also deals the attacker's combat damage to the defending player.
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1 - combatDamage);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent survivors = addReadySurvivors(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(survivors.isTapped()).isTrue();
        assertThat(survivors.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert deals no ability damage and gains no life")
    void decliningExertDoesNothing() {
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        Permanent survivors = addReadySurvivors(player1);
        int combatDamage = gqs.getEffectivePower(gd, survivors);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        // Only combat damage, no ability damage/life gain.
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - combatDamage);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(survivors.getSkipUntapCount()).isZero();
    }

    // ===== Helpers =====

    private Permanent addReadySurvivors(Player player) {
        return addCreatureReady(player, new ResoluteSurvivors());
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
