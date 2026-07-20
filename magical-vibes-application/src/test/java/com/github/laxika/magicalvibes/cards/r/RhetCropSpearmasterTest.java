package com.github.laxika.magicalvibes.cards.r;

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

class RhetCropSpearmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+0 and first strike until end of turn")
    void exertBoostsAndGrantsFirstStrike() {
        Permanent spearmaster = addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, spearmaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spearmaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spearmaster, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent spearmaster = addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(spearmaster.isTapped()).isTrue();
        assertThat(spearmaster.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and grants no first strike")
    void decliningExertDoesNothing() {
        Permanent spearmaster = addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, spearmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spearmaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spearmaster, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(spearmaster.getSkipUntapCount()).isZero();
    }

    // ===== Helpers =====

    private Permanent addReadySpearmaster(Player player) {
        return addCreatureReady(player, new RhetCropSpearmaster());
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
