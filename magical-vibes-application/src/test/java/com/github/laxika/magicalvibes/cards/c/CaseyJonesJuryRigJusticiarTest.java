package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castCasey(List<Card> library) {
        harness.setHand(player1, List.of(new CaseyJonesJuryRigJusticiar()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
