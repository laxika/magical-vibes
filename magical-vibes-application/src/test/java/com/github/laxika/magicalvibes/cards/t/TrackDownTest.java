package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrackDownTest extends BaseCardTest {

    private void castTrackDown() {
        harness.setHand(player1, List.of(new TrackDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void keepAllThreeOnTop() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
    }

    @Test
    @DisplayName("Resolving Track Down starts a scry 3 interaction")
    void startsScryThree() {
        harness.setLibrary(player1, List.of(new Forest(), new Shock(), new GrizzlyBears(), new Forest()));

        castTrackDown();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("Revealed creature card is drawn")
    void drawsRevealedCreature() {
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setLibrary(player1, List.of(forest, creature, noncreature, new Forest()));

        castTrackDown();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0, 2), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Revealed land card is drawn")
    void drawsRevealedLand() {
        Card nonland = new Shock();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland, land, creature, new Forest()));

        castTrackDown();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0, 2), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Revealed noncreature nonland card stays on top without drawing")
    void noncreatureNonlandStaysOnTop() {
        Card noncreature = new Shock();
        harness.setLibrary(player1, List.of(noncreature, new Forest(), new GrizzlyBears(), new Forest()));

        castTrackDown();
        keepAllThreeOnTop();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(noncreature);
    }
}
