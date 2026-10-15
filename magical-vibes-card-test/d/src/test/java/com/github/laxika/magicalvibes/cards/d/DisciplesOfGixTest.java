package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.c.ClayRevenant;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PollutedCisternDimOubliette;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Disciples of Gix")
@CardUsed({DisciplesOfGix.class, ChromaticStar.class, GrizzlyBears.class,
        IchorWellspring.class, Ornithopter.class, PollutedCisternDimOubliette.class,
        ClayRevenant.class, EnergyRefractor.class})
class DisciplesOfGixTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts up to three artifact cards from the library into the graveyard")
    void etbPutsUpToThreeArtifactsIntoGraveyard() {
        Card first = new ChromaticStar();
        Card second = new IchorWellspring();
        Card third = new Ornithopter();
        Card fourth = new GrizzlyBears();
        castWithLibrary(List.of(first, second, third, fourth));

        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

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

        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

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

        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(artifact.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(nonArtifact.getId());
    }

    @Test
    void searchMayFindZeroArtifacts() {
        Card artifact = new EnergyRefractor();
        castWithLibrary(List.of(artifact));
        resolveAllTriggers();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void emptyLibraryStillCompletesAndShuffles() {
        castWithLibrary(List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void libraryWithoutArtifactsStillCompletesAndShuffles() {
        Card creature = new DisciplesOfGix();
        castWithLibrary(List.of(creature));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void searchStopsAtThreeEvenWhenMoreArtifactsRemain() {
        Card first = new EnergyRefractor();
        Card second = new EnergyRefractor();
        Card third = new EnergyRefractor();
        Card fourth = new EnergyRefractor();
        castWithLibrary(List.of(first, second, third, fourth));
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void foundArtifactsEnterGraveyardTogetherForOneOrMoreTriggers() {
        harness.setHand(player1, List.of(new PollutedCisternDimOubliette()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castModalSorcery(player1, 0, 0, List.of());
        resolveAllTriggers();
        int opponentLife = gd.getLife(player2.getId());
        Card first = new EnergyRefractor();
        Card second = new EnergyRefractor();
        Card third = new ClayRevenant();
        castWithLibrary(List.of(first, second, third));
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 2);
    }

    private void castWithLibrary(List<Card> library) {
        harness.setHand(player1, List.of(new DisciplesOfGix()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.setLibrary(player1, library);
    }

}
