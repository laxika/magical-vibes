package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastriderVanguard.class, LightningStrike.class, Plains.class})
class BeastriderVanguardTest extends BaseCardTest {

    @Test
    void offersOnlyPermanentCardsFromTopThree() {
        Card permanent = new BeastriderVanguard();
        Card instant = new LightningStrike();
        Card land = new Plains();
        setLibrary(permanent, instant, land);
        activateAbility();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(permanent, land);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    void chosenPermanentGoesToHandAndRestIsOrderedOnBottom() {
        Card permanent = new BeastriderVanguard();
        Card instant = new LightningStrike();
        Card land = new Plains();
        Card belowTopThree = new BeastriderVanguard();
        setLibrary(permanent, instant, land, belowTopThree);
        activateAbility();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(permanent);
        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(instant, land);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowTopThree, land, instant);
    }

    @Test
    void mayDeclineAndPutAllLookedAtCardsOnBottom() {
        Card permanent = new BeastriderVanguard();
        Card instant = new LightningStrike();
        Card land = new Plains();
        setLibrary(permanent, instant, land);
        activateAbility();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(permanent, instant, land);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(permanent, instant, land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, permanent, instant);
    }

    @Test
    void canChooseALandWithFewerThanThreeCardsInLibrary() {
        Card instant = new LightningStrike();
        Card land = new Plains();
        setLibrary(instant, land);
        activateAbility();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(land).doesNotContain(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("reveals Plains and puts it into their hand")).isTrue();
    }

    @Test
    void mayDeclineTheOnlyCardInLibrary() {
        Card land = new Plains();
        setLibrary(land);
        activateAbility();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noEligibleCardsStillAllowsOrderingTheTopThreeOnBottom() {
        Card first = new LightningStrike();
        Card second = new LightningStrike();
        Card third = new LightningStrike();
        Card fourth = new Plains();
        setLibrary(first, second, third, fourth);
        activateAbility();

        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second, third, fourth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoiceOrDrawing() {
        setLibrary();
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));
        activateAbility();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void activateAbility() {
        harness.addToBattlefield(player1, new BeastriderVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
