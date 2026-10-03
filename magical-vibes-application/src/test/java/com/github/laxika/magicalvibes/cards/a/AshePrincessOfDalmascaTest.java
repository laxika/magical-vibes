package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.s.ShimmerMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshePrincessOfDalmasca.class, ShimmerMyr.class, GrizzlyBears.class, Forest.class, IronGiant.class})
class AshePrincessOfDalmascaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers an artifact among the top five cards")
    void attackingOffersArtifactAmongTopFive() {
        Card artifact = new ShimmerMyr();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), artifact, new Forest(), new GrizzlyBears(), new Forest()));

        declareAttack();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand and bottoms the rest")
    void choosingArtifactPutsItIntoHand() {
        Card artifact = new ShimmerMyr();
        List<Card> otherCards = List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest());
        harness.setLibrary(player1, List.of(artifact, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3)));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(otherCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no artifact among the top five, all five cards are bottomed")
    void noArtifactBottomsAllFive() {
        List<Card> library = List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears());
        harness.setLibrary(player1, library);

        declareAttack();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    @DisplayName("Declining an artifact leaves the untouched library above all five looked-at cards")
    void decliningArtifactBottomsAllLookedAtCards() {
        Card artifact = new IronGiant();
        List<Card> lookedAt = List.of(artifact, new Forest(), new Forest(), new Forest(), new Forest());
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), untouched));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(lookedAt);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only one artifact is taken and cards below the top five stay above the remainder")
    void choosingAmongMultipleArtifactsPreservesUntouchedLibrary() {
        Card firstArtifact = new IronGiant();
        Card chosenArtifact = new IronGiant();
        Card land1 = new Forest();
        Card land2 = new Forest();
        Card land3 = new Forest();
        Card untouchedArtifact = new IronGiant();
        Card untouchedLand = new Forest();
        harness.setLibrary(player1, List.of(firstArtifact, land1, chosenArtifact, land2, land3,
                untouchedArtifact, untouchedLand));

        declareAttack();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstArtifact.getId(), chosenArtifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosenArtifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenArtifact)
                .doesNotContain(firstArtifact, untouchedArtifact);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(untouchedArtifact, untouchedLand);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrder(firstArtifact, land1, land2, land3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A library with fewer than five cards still offers an artifact")
    void shortLibraryStillOffersArtifact() {
        Card artifact = new IronGiant();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land, artifact));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The only card in the library can be an artifact and still be declined")
    void onlyArtifactCanBeDeclined() {
        Card artifact = new IronGiant();
        harness.setLibrary(player1, List.of(artifact));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library resolves without requesting a choice or drawing a card")
    void emptyLibraryRequiresNoChoice() {
        harness.setLibrary(player1, List.of());
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));

        declareAttack();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void declareAttack() {
        Permanent ashe = harness.addToBattlefieldAndReturn(player1, new AshePrincessOfDalmasca());
        ashe.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
    }
}
