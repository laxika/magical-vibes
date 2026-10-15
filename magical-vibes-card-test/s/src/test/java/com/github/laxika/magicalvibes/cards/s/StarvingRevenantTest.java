package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AcolyteOfAclazotz;
import com.github.laxika.magicalvibes.cards.a.AncestorsAid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarvingRevenant.class, AcolyteOfAclazotz.class, AncestorsAid.class})
class StarvingRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 2, then draws and loses life for each card kept on top")
    void surveilsThenDrawsAndLosesLifeForEachCardKept() {
        Card keptCard = new AcolyteOfAclazotz();
        Card rejectedCard = new AcolyteOfAclazotz();
        Card nextCard = new AcolyteOfAclazotz();
        harness.setLibrary(player1, List.of(keptCard, rejectedCard, nextCard));
        harness.castFromHand(player1, new StarvingRevenant(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rejectedCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Descend drains after drawing with eight permanent cards in the graveyard")
    void descendDrainsWithEightPermanentCards() {
        harness.setGraveyard(player1, permanentCards(8));
        harness.setLibrary(player1, List.of(new AcolyteOfAclazotz()));
        harness.addToBattlefield(player1, new StarvingRevenant());

        drawAndResolveTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Descend does not count nonpermanent cards")
    void descendDoesNotCountNonpermanentCards() {
        List<Card> cards = new ArrayList<>(permanentCards(7));
        cards.add(new AncestorsAid());
        harness.setGraveyard(player1, cards);
        harness.setLibrary(player1, List.of(new AcolyteOfAclazotz()));
        harness.addToBattlefield(player1, new StarvingRevenant());

        drawAndResolveTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void keepingBothCardsDrawsBothAndLosesSixLife() {
        Card first = new AcolyteOfAclazotz();
        Card second = new AcolyteOfAclazotz();
        harness.setLibrary(player1, List.of(first, second, new AcolyteOfAclazotz()));
        beginSurveil();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 14);
    }

    @Test
    void puttingBothCardsInGraveyardDrawsNothingAndLosesNoLife() {
        Card first = new AcolyteOfAclazotz();
        Card second = new AcolyteOfAclazotz();
        Card next = new AcolyteOfAclazotz();
        harness.setLibrary(player1, List.of(first, second, next));
        beginSurveil();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        harness.assertLife(player1, 20);
    }

    @Test
    void oneCardLibraryOnlyDrawsOneAndLosesThreeLife() {
        Card onlyCard = new AcolyteOfAclazotz();
        harness.setLibrary(player1, List.of(onlyCard));
        beginSurveil();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        harness.assertLife(player1, 17);
    }

    @Test
    void emptyLibrarySurveilsNothingAndDoesNotAttemptToDraw() {
        harness.setLibrary(player1, List.of());
        beginSurveil();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void surveilledPermanentEnablesDescendBeforeTheDraw() {
        harness.setGraveyard(player1, permanentCards(7));
        harness.setLibrary(player1, permanentCards(3));
        beginSurveil();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
        chooseOpponentIfPrompted();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
    }

    @Test
    void descendRequiresChoosingAnOpponentAsTarget() {
        harness.setGraveyard(player1, permanentCards(8));
        harness.setLibrary(player1, permanentCards(2));
        harness.addToBattlefield(player1, new StarvingRevenant());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void descendDoesNothingIfPermanentCountDropsBeforeResolution() {
        harness.setGraveyard(player1, permanentCards(8));
        harness.setLibrary(player1, permanentCards(2));
        harness.addToBattlefield(player1, new StarvingRevenant());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        chooseOpponentIfPrompted();

        harness.setGraveyard(player1, permanentCards(7));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void drawingBelowThresholdDoesNotTriggerEvenIfCountLaterIncreases() {
        harness.setGraveyard(player1, permanentCards(7));
        harness.setLibrary(player1, permanentCards(2));
        harness.addToBattlefield(player1, new StarvingRevenant());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.setGraveyard(player1, permanentCards(8));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsDrawDoesNotTriggerDescend() {
        harness.setGraveyard(player1, permanentCards(8));
        harness.setLibrary(player2, permanentCards(2));
        harness.addToBattlefield(player1, new StarvingRevenant());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void beginSurveil() {
        harness.castFromHand(player1, new StarvingRevenant(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void chooseOpponentIfPrompted() {
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
    }

    private void drawAndResolveTrigger() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        chooseOpponentIfPrompted();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private List<Card> permanentCards(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new AcolyteOfAclazotz())
                .toList();
    }
}
