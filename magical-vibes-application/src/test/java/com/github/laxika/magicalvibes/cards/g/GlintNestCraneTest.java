package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.SelectForInspection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlintNestCrane.class, DukharaPeafowl.class, Island.class,
        PropheticPrism.class, SelectForInspection.class})
class GlintNestCraneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only artifact cards among the top four")
    void etbOffersOnlyArtifacts() {
        Card artifact = card("Test Artifact", CardType.ARTIFACT);
        setupTopCards(List.of(artifact, card("Test Creature", CardType.CREATURE),
                card("Test Land", CardType.LAND), card("Test Sorcery", CardType.SORCERY)));

        castAndResolveEtb();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(artifact);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand and prompts to order the rest on the bottom")
    void choosingArtifactPutsItIntoHand() {
        Card artifact = card("Test Artifact", CardType.ARTIFACT);
        Card creature = card("Test Creature", CardType.CREATURE);
        Card land = card("Test Land", CardType.LAND);
        Card sorcery = card("Test Sorcery", CardType.SORCERY);
        setupTopCards(List.of(artifact, creature, land, sorcery));

        castAndResolveEtb();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyInAnyOrder(creature, land, sorcery);
    }

    @Test
    @DisplayName("With no artifact among the top four, all cards go to the bottom")
    void noArtifactNeedsNoCardChoice() {
        List<Card> topCards = List.of(card("Test Creature", CardType.CREATURE), card("Test Land", CardType.LAND),
                card("Test Sorcery", CardType.SORCERY), card("Test Instant", CardType.INSTANT));
        setupTopCards(topCards);

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    void mayDeclineArtifactAndOrderAllFourBelowUntouchedCards() {
        Card artifact = new PropheticPrism();
        Card creature = new GlintNestCrane();
        Card land = new Island();
        Card instant = new SelectForInspection();
        Card fifthCard = new DukharaPeafowl();
        harness.setLibrary(player1, List.of(artifact, creature, land, instant, fifthCard));

        castAndResolveEtb();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(artifact);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(fifthCard, instant, creature, artifact, land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosesOnlyOneArtifactIncludingArtifactCreatureAndOrdersRemainder() {
        Card artifact = new PropheticPrism();
        Card artifactCreature = new DukharaPeafowl();
        Card land = new Island();
        Card instant = new SelectForInspection();
        Card fifthCard = new PropheticPrism();
        harness.setLibrary(player1, List.of(artifact, artifactCreature, land, instant, fifthCard));

        castAndResolveEtb();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(artifact, artifactCreature);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifactCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifthCard, instant, artifact, land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noArtifactReturnsAllLookedAtCardsInChosenOrder() {
        Card creature = new GlintNestCrane();
        Card firstLand = new Island();
        Card instant = new SelectForInspection();
        Card secondLand = new Island();
        Card untouchedArtifact = new PropheticPrism();
        harness.setLibrary(player1, List.of(creature, firstLand, instant, secondLand, untouchedArtifact));

        castAndResolveEtb();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 3, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouchedArtifact, instant, secondLand, creature, firstLand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void shortLibraryAllowsSelectionAndReturnsRemainingCard() {
        Card land = new Island();
        Card artifact = new PropheticPrism();
        harness.setLibrary(player1, List.of(land, artifact));

        castAndResolveEtb();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void soleArtifactCanStillBeDeclined() {
        Card artifact = new PropheticPrism();
        harness.setLibrary(player1, List.of(artifact));

        castAndResolveEtb();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryCompletesWithoutChoiceOrDraw() {
        harness.setLibrary(player1, List.of());

        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.castFromHand(player1, new GlintNestCrane(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
