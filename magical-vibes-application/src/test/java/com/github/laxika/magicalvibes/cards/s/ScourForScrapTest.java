package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScourForScrap.class, Forest.class, GrizzlyBears.class, Millstone.class})
class ScourForScrapTest extends BaseCardTest {

    @Test
    void searchesLibraryForAnArtifact() {
        Card artifact = new Millstone();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), artifact));
        cast(new int[]{0}, null);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(artifact.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(artifact.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(artifact.getId());
    }

    @Test
    void returnsTargetArtifactFromGraveyard() {
        Card artifact = new Millstone();
        harness.setGraveyard(player1, List.of(artifact));
        cast(new int[]{1}, artifact.getId());

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(artifact.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(artifact.getId());
    }

    @Test
    void bothModesResolve() {
        Card artifactInLibrary = new Millstone();
        Card artifactInGraveyard = new Millstone();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), artifactInLibrary));
        harness.setGraveyard(player1, List.of(artifactInGraveyard));
        cast(new int[]{0, 1}, artifactInGraveyard.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(artifactInLibrary.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(artifactInLibrary.getId(), artifactInGraveyard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(artifactInGraveyard.getId());
    }

    @Test
    void cannotReturnNonArtifactFromGraveyard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        assertThatThrownBy(() -> cast(new int[]{1}, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, java.util.UUID graveyardTargetId) {
        harness.setHand(player1, List.of(new ScourForScrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, graveyardTargetId, List.of());
        harness.passBothPriorities();
    }
}
