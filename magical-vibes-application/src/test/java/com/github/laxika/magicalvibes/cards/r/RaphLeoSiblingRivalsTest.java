package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphLeoSiblingRivals.class, GrizzlyBears.class})
class RaphLeoSiblingRivalsTest extends BaseCardTest {

    @Test
    @DisplayName("First attack untaps one or two attacking creatures and grants another combat")
    void firstAttackUntapsChosenAttackersAndGrantsExtraCombat() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(raphLeo.getId(), bear.getId());

        harness.handlePermanentChosen(player1, raphLeo.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(raphLeo.isTapped()).isFalse();
        assertThat(bear.isTapped()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("The target choice only offers attacking creatures")
    void targetChoiceOnlyOffersAttackingCreatures() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0), 1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(raphLeo.getId())
                .doesNotContain(nonAttacker.getId());
        harness.handlePermanentChosen(player1, raphLeo.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Attacking in a later combat phase does not grant another combat")
    void laterCombatAttackDoesNothing() {
        Permanent raphLeo = addCreatureReady(player1, new RaphLeoSiblingRivals());

        declareAttackers(player1, List.of(0), 2);

        assertThat(gd.stack).isEmpty();
        assertThat(raphLeo.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
