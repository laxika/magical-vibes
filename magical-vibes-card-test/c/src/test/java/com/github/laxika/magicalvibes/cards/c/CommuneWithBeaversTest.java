package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.Ether;
import com.github.laxika.magicalvibes.cards.f.FireMagic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.q.QutrubForayer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommuneWithBeavers.class, Ether.class, Forest.class, QutrubForayer.class, FireMagic.class})
class CommuneWithBeaversTest extends BaseCardTest {

    @Test
    void offersArtifactCreatureAndLandCardsFromTheTopThree() {
        Card artifact = new Ether();
        Card creature = new QutrubForayer();
        Card land = new Forest();
        Card instant = new FireMagic();
        setLibrary(artifact, creature, land, instant);

        castAndResolve();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactlyInAnyOrder(artifact, creature, land);
        assertThat(choice.params().canFailToFind()).isTrue();
    }

    @Test
    void putsChosenCardIntoHandAndOrdersTheRestOnTheBottom() {
        Card artifact = new Ether();
        Card creature = new QutrubForayer();
        Card land = new Forest();
        setLibrary(artifact, creature, land);

        castAndResolve();
        harness.handleCardChosen(player1, 1);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineAndPutAllLookedAtCardsOnTheBottom() {
        Card artifact = new Ether();
        Card creature = new QutrubForayer();
        Card land = new Forest();
        setLibrary(artifact, creature, land);

        castAndResolve();
        harness.handleCardChosen(player1, -1);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, creature, artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTakeAnArtifactAndLeavesTheFourthCardAboveTheBottomedCards() {
        Card instant = new FireMagic();
        Card artifact = new Ether();
        Card otherInstant = new FireMagic();
        Card fourth = new Forest();
        setLibrary(instant, artifact, otherInstant, fourth);

        castAndResolve();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(artifact);
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("reveals " + artifact.getName()));
        harness.handleCardChosen(player1, 0);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals " + artifact.getName()));
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, otherInstant, instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTakeALandFromALibraryWithFewerThanThreeCards() {
        Card instant = new FireMagic();
        Card land = new Forest();
        setLibrary(instant, land);

        castAndResolve();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineEvenWhenTheLibraryContainsOnlyOneEligibleCard() {
        Card land = new Forest();
        setLibrary(land);

        castAndResolve();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void ordersAllThreeCardsOnTheBottomWhenNoneIsEligible() {
        Card first = new FireMagic();
        Card second = new FireMagic();
        Card third = new FireMagic();
        Card fourth = new Forest();
        setLibrary(first, second, third, fourth);

        castAndResolve();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        setLibrary();

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new CommuneWithBeavers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
