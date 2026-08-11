package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrognosticSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants hexproof and taps Prognostic Sphinx")
    void discardGrantsHexproofAndTapsSphinx() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new PrognosticSphinx());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(sphinx.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The discard ability can be activated while Prognostic Sphinx is tapped")
    void discardAbilityDoesNotRequireUntappedSphinx() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new PrognosticSphinx());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(sphinx.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Attacking with Prognostic Sphinx triggers scry 3")
    void attackingTriggersScryThree() {
        Permanent sphinx = addCreatureReady(player1, new PrognosticSphinx());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(3);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(sphinx.isTapped()).isTrue();
    }
}
