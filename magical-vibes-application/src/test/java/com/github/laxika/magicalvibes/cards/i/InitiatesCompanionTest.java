package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InitiatesCompanionTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTappedPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.tap();
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Combat damage to a player prompts a choice of any creature or land, excluding other permanents")
    void promptsToChooseCreatureOrLand() {
        Permanent companion = addReadyCreature(player1, new InitiatesCompanion());
        companion.setAttacking(true);
        Permanent ownLand = addTappedPermanent(player1, new Forest());
        Permanent enemyCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent enemyArtifact = addTappedPermanent(player2, new HowlingMine());

        resolveCombat();
        harness.passBothPriorities(); // resolve the untap trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(ownLand.getId(), enemyCreature.getId())
                .doesNotContain(enemyArtifact.getId());
    }

    @Test
    @DisplayName("The chosen creature is untapped and the game advances")
    void untapsChosenCreature() {
        Permanent companion = addReadyCreature(player1, new InitiatesCompanion());
        companion.setAttacking(true);
        Permanent tappedCreature = addReadyCreature(player1, new GrizzlyBears());
        tappedCreature.tap();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(tappedCreature.getId()));

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("The chosen land is untapped")
    void untapsChosenLand() {
        Permanent companion = addReadyCreature(player1, new InitiatesCompanion());
        companion.setAttacking(true);
        Permanent tappedLand = addTappedPermanent(player1, new Forest());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(tappedLand.getId()));

        assertThat(tappedLand.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
