package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ChromeCourier.class)
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

    private void castCourier() {
        harness.setHand(player1, List.of(new ChromeCourier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
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
