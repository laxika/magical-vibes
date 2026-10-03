package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseyJonesJuryRigJusticiar.class, FountainOfYouth.class, Shock.class})
class CaseyJonesJuryRigJusticiarTest extends BaseCardTest {

    @Test
    void mayRevealAnArtifactIntoHand() {
        Card artifact = new FountainOfYouth();
        List<Card> library = List.of(artifact, new Shock(), new Shock(), new Shock());
        castCasey(library);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(1, library.size()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningTheArtifactPutsAllFourCardsOnTheBottom() {
        Card artifact = new FountainOfYouth();
        List<Card> library = List.of(artifact, new Shock(), new Shock(), new Shock());
        castCasey(library);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void withNoArtifactAllFourCardsGoOnTheBottomWithoutAChoice() {
        List<Card> library = List.of(new Shock(), new Shock(), new Shock(), new Shock());
        castCasey(library);

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseOnlyOneOfMultipleArtifacts() {
        Card firstArtifact = new FountainOfYouth();
        Card secondArtifact = new FountainOfYouth();
        Card nonArtifact = new Shock();
        List<Card> library = List.of(firstArtifact, nonArtifact, secondArtifact, new Shock());
        castCasey(library);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(secondArtifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondArtifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstArtifact, nonArtifact, library.get(3));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void unlookedAtCardsStayAboveTheCardsPutOnTheBottom() {
        Card artifact = new FountainOfYouth();
        Card firstRemaining = new Shock();
        Card secondRemaining = new Shock();
        List<Card> library = List.of(artifact, new Shock(), new Shock(), new Shock(),
                firstRemaining, secondRemaining);
        castCasey(library);

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.subList(0, 2)).containsExactly(firstRemaining, secondRemaining);
        assertThat(deck.subList(2, 5))
                .containsExactlyInAnyOrderElementsOf(library.subList(1, 4));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void artifactBelowTheTopFourCannotBeTaken() {
        Card artifact = new FountainOfYouth();
        List<Card> library = List.of(new Shock(), new Shock(), new Shock(), new Shock(), artifact);
        castCasey(library);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.getFirst()).isSameAs(artifact);
        assertThat(deck.subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 4));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineTheOnlyCardInAShortLibrary() {
        Card artifact = new FountainOfYouth();
        castCasey(List.of(artifact));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayTakeAnArtifactFromAShortLibrary() {
        Card artifact = new FountainOfYouth();
        Card otherCard = new Shock();
        castCasey(List.of(otherCard, artifact));

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryFinishesWithoutAChoice() {
        castCasey(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castCasey(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new CaseyJonesJuryRigJusticiar(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
