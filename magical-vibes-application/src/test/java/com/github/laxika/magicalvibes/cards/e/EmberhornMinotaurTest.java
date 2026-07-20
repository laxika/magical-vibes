package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmberhornMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+1 and menace until end of turn")
    void exertBoostsAndGrantsMenace() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(minotaur.isTapped()).isTrue();
        assertThat(minotaur.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and grants no menace")
    void decliningExertDoesNothing() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.MENACE)).isFalse();
        assertThat(minotaur.getSkipUntapCount()).isZero();
    }

    // ===== Helpers =====

    private Permanent addReadyMinotaur(Player player) {
        return addCreatureReady(player, new EmberhornMinotaur());
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
