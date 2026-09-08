package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Disciples of Gix")
class DisciplesOfGixTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts up to three artifact cards from the library into the graveyard")
    void etbPutsUpToThreeArtifactsIntoGraveyard() {
        Card first = new ChromaticStar();
        Card second = new IchorWellspring();
        Card third = new Ornithopter();
        Card fourth = new GrizzlyBears();
        castWithLibrary(List.of(first, second, third, fourth));

        resolveTrigger();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(fourth.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The up-to search may stop before finding three artifacts")
    void searchMayStopEarly() {
        Card first = new ChromaticStar();
        Card second = new IchorWellspring();
        Card third = new Ornithopter();
        castWithLibrary(List.of(first, second, third));

        resolveTrigger();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(second.getId(), third.getId());
    }

    @Test
    @DisplayName("Only artifact cards are offered by the ETB search")
    void searchOnlyOffersArtifacts() {
        Card artifact = new ChromaticStar();
        Card nonArtifact = new GrizzlyBears();
        castWithLibrary(List.of(nonArtifact, artifact));

        resolveTrigger();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(artifact.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(nonArtifact.getId());
    }

    private void castWithLibrary(List<Card> library) {
        harness.setHand(player1, List.of(new DisciplesOfGix()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.setLibrary(player1, library);
    }

    private void resolveTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
