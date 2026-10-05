package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathOfTheEnigma.class, Panopticon.class, Forest.class, GrizzlyBears.class})
class PathOfTheEnigmaTest extends BaseCardTest {

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
    void targetPlayerDrawsFourAndPlaneswalksOnAPlaneswalkMajority() {
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, cards);
        int targetHandBefore = gd.playerHands.get(player2.getId()).size();
        cast(player2.getId());

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(targetHandBefore + 4)
                .containsAll(cards);
        assertThat(gd.planechase.faceUp)
                .noneMatch(object -> object == startingPlane)
                .hasSize(1);
    }

    @Test
    void tiedVoteCausesChaos() {
        Forest chaosDraw = new Forest();
        harness.setLibrary(player1, List.of(chaosDraw));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        cast(player2.getId());

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(gd.playerHands.get(player1.getId())).contains(chaosDraw);
    }

    @Test
    void canTargetItsControllerAndDrawsBeforeVoting() {
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, cards);
        cast(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
    }

    @Test
    void chaosMajorityTriggersChaosWithoutPlaneswalking() {
        Forest chaosDraw = new Forest();
        harness.setLibrary(player1, List.of(chaosDraw));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        cast(player2.getId());

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chaosDraw);
    }

    @Test
    void drawsAndCompletesVotingOutsidePlanechase() {
        gd.planechase = null;
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, cards);
        int targetHandBefore = gd.playerHands.get(player2.getId()).size();
        cast(player2.getId());

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(targetHandBefore + 4).containsAll(cards);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Path of the Enigma");
    }

    @Test
    void cannotTargetAPermanent() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PathOfTheEnigma()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new PathOfTheEnigma()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options())
                .containsExactlyElementsOf(ChoiceContext.WillOfThePlaneswalkersChoice.OPTIONS);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
    }
}
