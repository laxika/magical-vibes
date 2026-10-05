package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfTheGhosthunter.class, Panopticon.class})
class PathOfTheGhosthunterTest extends BaseCardTest {

    private PlanarObject startingPlane;

    @BeforeEach
    void preparePlanechase() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        startingPlane = new PlanarObject(new Panopticon(), gd.nextTimestamp());
        gd.planechase.faceUp.add(startingPlane);
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void createsXSpiritsAndPlaneswalksOnAPlaneswalkMajority() {
        cast(2);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(gd.planechase.faceUp)
                .noneMatch(object -> object == startingPlane)
                .hasSize(1);
    }

    @Test
    void aTiedVoteCausesChaos() {
        PathOfTheGhosthunter drawn = new PathOfTheGhosthunter();
        harness.setLibrary(player1, List.of(drawn));
        cast(1);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void aChaosMajorityTriggersTheCurrentPlane() {
        PathOfTheGhosthunter drawn = new PathOfTheGhosthunter();
        harness.setLibrary(player1, List.of(drawn));
        cast(2);

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void zeroXStillVotesAndCausesChaos() {
        PathOfTheGhosthunter drawn = new PathOfTheGhosthunter();
        harness.setLibrary(player1, List.of(drawn));
        cast(0);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
    }

    @Test
    void outsidePlanechaseStillCreatesTokensAndCompletesVoting() {
        gd.planechase = null;
        cast(3);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        assertThat(findPermanents(player1, "Spirit")).hasSize(3);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Path of the Ghosthunter");
    }

    private void cast(int xValue) {
        harness.setHand(player1, List.of(new PathOfTheGhosthunter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 1);
        harness.castAndResolveSorcery(player1, 0, xValue);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options())
                .containsExactlyElementsOf(ChoiceContext.WillOfThePlaneswalkersChoice.OPTIONS);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
    }
}
