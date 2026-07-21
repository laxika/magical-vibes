package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KhenraScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyScrapper(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +2/+0 until end of turn")
    void exertBoosts() {
        Permanent scrapper = addReadyScrapper(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, scrapper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, scrapper)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent scrapper = addReadyScrapper(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(scrapper.isTapped()).isTrue();
        assertThat(scrapper.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent scrapper = addReadyScrapper(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, scrapper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scrapper)).isEqualTo(3);
        assertThat(scrapper.getSkipUntapCount()).isZero();
    }

    // ===== Helpers =====

    private Permanent addReadyScrapper(Player player) {
        return addCreatureReady(player, new KhenraScrapper());
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
