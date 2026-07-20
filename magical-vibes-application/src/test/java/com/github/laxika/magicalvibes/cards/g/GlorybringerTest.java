package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.v.VerixBladewing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlorybringerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues the exert trigger for target selection")
    void attackQueuesTargetSelection() {
        addCreatureReady(player1, new Glorybringer());
        addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Exerting deals 4 damage to the target and skips the dragon's next untap")
    void exertDealsFourDamageAndSkipsUntap() {
        Permanent glorybringer = addCreatureReady(player1, new Glorybringer());
        Permanent dreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, dreadmaw.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dreadmaw.getMarkedDamage()).isEqualTo(4);
        assertThat(glorybringer.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert deals no damage and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent glorybringer = addCreatureReady(player1, new Glorybringer());
        Permanent dreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, dreadmaw.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dreadmaw.getMarkedDamage()).isZero();
        assertThat(glorybringer.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Only non-Dragon creatures an opponent controls are legal targets")
    void targetFilterExcludesDragonsAndOwnCreatures() {
        addCreatureReady(player1, new Glorybringer());
        Permanent ownDreadmaw = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent opponentDreadmaw = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent opponentDragon = addCreatureReady(player2, new VerixBladewing());

        declareAttackers(List.of(0));

        PendingInteraction interaction = gd.interaction.activeInteraction();
        assertThat(interaction).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) interaction;

        assertThat(choice.validIds()).contains(opponentDreadmaw.getId());
        assertThat(choice.validIds()).doesNotContain(opponentDragon.getId(), ownDreadmaw.getId());
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
