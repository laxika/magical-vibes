package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BarbedBattlegear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OltecArchaeologists.class, BarbedBattlegear.class, Forest.class, GrizzlyBears.class})
class OltecArchaeologistsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a targeted artifact card from the graveyard to hand")
    void returnsArtifactFromGraveyardToHand() {
        Card artifact = new BarbedBattlegear();
        Card nonArtifact = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, nonArtifact));

        castOltecArchaeologists(0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonArtifact);
    }

    @Test
    @DisplayName("The graveyard mode has no legal choice when no artifact is present")
    void doesNotTargetNonArtifactCards() {
        Card nonArtifact = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonArtifact));

        castOltecArchaeologists(0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonArtifact);
    }

    @Test
    @DisplayName("Scry mode scries three cards")
    void scriesThree() {
        Card top = new Forest();
        Card middle = new GrizzlyBears();
        Card bottom = new BarbedBattlegear();
        harness.setLibrary(player1, List.of(top, middle, bottom));

        castOltecArchaeologists(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top, middle, bottom);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, middle, top);
    }

    private void castOltecArchaeologists(int mode) {
        harness.setHand(player1, List.of(new OltecArchaeologists()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
