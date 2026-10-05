package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrognosticSphinx.class, NessianCourser.class})
class PrognosticSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants hexproof and taps Prognostic Sphinx")
    void discardGrantsHexproofAndTapsSphinx() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new PrognosticSphinx());
        harness.setHand(player1, List.of(new NessianCourser()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nessian Courser");
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
        harness.setHand(player1, List.of(new NessianCourser(), new NessianCourser()));

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
                new NessianCourser(),
                new NessianCourser(),
                new NessianCourser(),
                new NessianCourser()
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

    @Test
    @DisplayName("Discard is paid before resolution, but hexproof and tapping wait for resolution")
    void discardIsAnActivationCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new PrognosticSphinx());
        harness.setHand(player1, List.of(new NessianCourser()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Nessian Courser");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(sphinx.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();

        harness.passBothPriorities();

        assertThat(sphinx.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("An empty hand cannot pay the discard activation cost")
    void cannotActivateWithEmptyHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new PrognosticSphinx());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(sphinx.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Scry uses the entire library when fewer than three cards remain")
    void scryWithShortLibrary() {
        addCreatureReady(player1, new PrognosticSphinx());
        NessianCourser first = new NessianCourser();
        NessianCourser second = new NessianCourser();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
