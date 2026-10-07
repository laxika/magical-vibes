package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrandOssuary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StartTheTARDIS.class, Island.class, GrandOssuary.class, GrizzlyBears.class})
class StartTheTARDISTest extends BaseCardTest {

    @Test
    void surveilsThenDrawsAndCanDeclineToPlaneswalk() {
        Card surveilledCard = new Island();
        Card drawnCard = new Island();
        StartTheTARDIS spell = new StartTheTARDIS();
        harness.setLibrary(player1, List.of(surveilledCard, drawnCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveilledCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void canPlaneswalkAfterSurveilingAndDrawing() {
        GrandOssuary departingPlane = new GrandOssuary();
        GrandOssuary arrivingPlane = new GrandOssuary();
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(arrivingPlane);
        gd.planechase.faceUp.add(new PlanarObject(departingPlane, gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new StartTheTARDIS()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getCard).containsExactly(arrivingPlane);
        assertThat(gd.planechase.deck).contains(departingPlane);
    }

    @Test
    void jumpStartDiscardsAndExilesTheSpell() {
        StartTheTARDIS spell = new StartTheTARDIS();
        Card discard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castJumpStart(player1, 0, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard).doesNotContain(spell);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void drawsTheFirstCardInTheChosenSurveilOrder() {
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StartTheTARDIS()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canPutBothSurveilledCardsIntoTheGraveyardBeforeDrawing() {
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StartTheTARDIS()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void surveilsAndDrawsWithOnlyOneCardInTheLibrary() {
        Card remaining = new Island();
        harness.setLibrary(player1, List.of(remaining));
        harness.setHand(player1, List.of(new StartTheTARDIS()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotJumpStartWithoutACardToDiscard() {
        StartTheTARDIS spell = new StartTheTARDIS();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.stack).isEmpty();
    }
}
