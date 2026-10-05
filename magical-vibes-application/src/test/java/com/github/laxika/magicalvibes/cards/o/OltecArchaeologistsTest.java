package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CogworkWrestler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MineshaftSpider;
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

@CardUsed({OltecArchaeologists.class, CogworkWrestler.class, Forest.class, MineshaftSpider.class})
class OltecArchaeologistsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a targeted artifact card from the graveyard to hand")
    void returnsArtifactFromGraveyardToHand() {
        Card artifact = new CogworkWrestler();
        Card nonArtifact = new MineshaftSpider();
        Card opponentArtifact = new CogworkWrestler();
        harness.setGraveyard(player1, List.of(artifact, nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        castOltecArchaeologists(0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
    }

    @Test
    @DisplayName("Scry can be chosen on entry without casting when the graveyard has no artifact")
    void choosesScryOnEntryWithoutArtifacts() {
        Card nonArtifact = new MineshaftSpider();
        Card top = new Forest();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.setLibrary(player1, List.of(top));

        harness.enterBattlefieldAndReturn(player1, new OltecArchaeologists());
        harness.handleListChoice(player1, "Scry 3");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonArtifact);
    }

    @Test
    @DisplayName("Scry mode scries three cards")
    void scriesThree() {
        Card top = new Forest();
        Card middle = new MineshaftSpider();
        Card bottom = new CogworkWrestler();
        harness.setLibrary(player1, List.of(top, middle, bottom));

        castOltecArchaeologists(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top, middle, bottom);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, middle, top);
    }

    @Test
    @DisplayName("Chooses the mode after the creature spell resolves")
    void choosesModeWhenTriggerGoesOnStack() {
        Card artifact = new CogworkWrestler();
        Card top = new Forest();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(top));

        harness.castFromHand(player1, new OltecArchaeologists(), "{4}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Scry 3");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    @DisplayName("Scry can put cards on the bottom below the untouched library")
    void putsScryCardsOnBottom() {
        Card first = new Forest();
        Card second = new MineshaftSpider();
        Card third = new CogworkWrestler();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, untouched));

        castOltecArchaeologists(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(2, 0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, untouched, third, first);
    }

    @Test
    @DisplayName("Scry with an empty library finishes without a card choice")
    void scriesEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castOltecArchaeologists(1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact target that leaves the graveyard is not returned")
    void doesNotReturnMissingTarget() {
        Card artifact = new CogworkWrestler();
        harness.setGraveyard(player1, List.of(artifact));

        castOltecArchaeologists(0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.stack).isEmpty();
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
