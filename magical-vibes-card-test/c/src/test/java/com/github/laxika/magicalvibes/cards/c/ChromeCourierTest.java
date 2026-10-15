package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChromeCourier.class, Counterspell.class})
class ChromeCourierTest extends BaseCardTest {

    @Test
    void putsOneCardIntoHandAndTheOtherIntoGraveyard() {
        Card artifact = card("Test Artifact", CardType.ARTIFACT);
        Card land = card("Test Land", CardType.LAND);
        harness.setLibrary(player1, List.of(artifact, land));

        castCourier();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
    }

    @Test
    void gainsThreeLifeWhenTheChosenCardIsAnArtifact() {
        Card artifact = card("Test Artifact", CardType.ARTIFACT);
        Card land = card("Test Land", CardType.LAND);
        harness.setLibrary(player1, List.of(land, artifact));
        int lifeBefore = gd.getLife(player1.getId());

        castCourier();

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
    }

    @Test
    void doesNotGainLifeWhenTheChosenCardIsNotAnArtifact() {
        Card land = card("Test Land", CardType.LAND);
        Card creature = card("Test Creature", CardType.CREATURE);
        harness.setLibrary(player1, List.of(land, creature));
        int lifeBefore = gd.getLife(player1.getId());

        castCourier();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
    }

    @Test
    void putsTheOnlyLibraryCardIntoHandAndGainsLifeIfItIsAnArtifact() {
        Card artifact = new ChromeCourier();
        harness.setLibrary(player1, List.of(artifact));
        int lifeBefore = gd.getLife(player1.getId());

        castCourier();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    @Test
    void emptyLibraryDoesNotGainLifeOrRequireAChoice() {
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        castCourier();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void revealsOnlyTwoCardsAndGainsLifeOnlyOnceWhenBothAreArtifacts() {
        Card first = new ChromeCourier();
        Card second = new ChromeCourier();
        Card third = new ChromeCourier();
        harness.setLibrary(player1, List.of(first, second, third));
        int lifeBefore = gd.getLife(player1.getId());

        castCourier();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void doesNotGainLifeForAnArtifactPutIntoTheGraveyard() {
        Card artifact = new ChromeCourier();
        Card instant = new Counterspell();
        harness.setLibrary(player1, List.of(artifact, instant));
        int lifeBefore = gd.getLife(player1.getId());

        castCourier();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    private void castCourier() {
        harness.castFromHand(player1, new ChromeCourier(), "{1}{W}{U}");
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
