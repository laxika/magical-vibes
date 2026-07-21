package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VizierOfTheTrueTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues the exert trigger for target selection")
    void attackQueuesTargetSelection() {
        addCreatureReady(player1, new VizierOfTheTrue());
        addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Exerting taps the target and skips the Vizier's next untap")
    void exertTapsTargetAndSkipsUntap() {
        Permanent vizier = addCreatureReady(player1, new VizierOfTheTrue());
        Permanent dreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, dreadmaw.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dreadmaw.isTapped()).isTrue();
        assertThat(vizier.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert taps nothing and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent vizier = addCreatureReady(player1, new VizierOfTheTrue());
        Permanent dreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, dreadmaw.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dreadmaw.isTapped()).isFalse();
        assertThat(vizier.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Only creatures an opponent controls are legal targets")
    void targetFilterExcludesOwnCreatures() {
        addCreatureReady(player1, new VizierOfTheTrue());
        Permanent ownDreadmaw = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent opponentDreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));

        PendingInteraction interaction = gd.interaction.activeInteraction();
        assertThat(interaction).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) interaction;

        assertThat(choice.validIds()).contains(opponentDreadmaw.getId());
        assertThat(choice.validIds()).doesNotContain(ownDreadmaw.getId());
    }

    // ===== Helpers =====

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
