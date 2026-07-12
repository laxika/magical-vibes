package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KinscaerHarpoonistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues attack trigger for target selection")
    void attackQueuesTargetSelection() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        addCreatureReady(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting the may ability makes the target creature lose flying")
    void acceptingRemovesFlying() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, hawk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining the may ability leaves flying intact")
    void decliningKeepsFlying() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, hawk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying is restored at end of turn")
    void flyingWearsOff() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, hawk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isFalse();

        // Clear the pending blocker declaration so the turn can advance to cleanup.
        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
