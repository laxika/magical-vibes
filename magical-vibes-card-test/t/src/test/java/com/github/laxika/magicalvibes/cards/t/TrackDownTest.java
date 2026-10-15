package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarVisionary;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrackDown.class, Forest.class, LlanowarVisionary.class, Shock.class})
class TrackDownTest extends BaseCardTest {

    private void castTrackDown() {
        harness.castFromHand(player1, new TrackDown(), "{1}{G}");
        harness.passBothPriorities();
    }

    private void keepAllThreeOnTop() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
    }

    @Test
    @DisplayName("Resolving Track Down starts a scry 3 interaction")
    void startsScryThree() {
        harness.setLibrary(player1, List.of(new Forest(), new Shock(), new LlanowarVisionary(), new Forest()));

        castTrackDown();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("Revealed creature card is drawn")
    void drawsRevealedCreature() {
        Card forest = new Forest();
        Card creature = new LlanowarVisionary();
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
        Card creature = new LlanowarVisionary();
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
        harness.setLibrary(player1, List.of(noncreature, new Forest(), new LlanowarVisionary(), new Forest()));

        castTrackDown();
        keepAllThreeOnTop();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(noncreature);
    }

    @Test
    @DisplayName("Bottoming all three cards reveals and draws the previously fourth card")
    void drawsFourthCardAfterBottomingAllThree() {
        Card first = new Shock();
        Card second = new Forest();
        Card third = new Shock();
        Card fourth = new LlanowarVisionary();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castTrackDown();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A one-card library can be bottomed and its land is still drawn")
    void drawsOnlyCardAfterBottomingIt() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castTrackDown();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(land);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library completes resolution without attempting a draw")
    void emptyLibraryDoesNotDraw() {
        harness.setLibrary(player1, List.of());

        castTrackDown();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Track Down");
    }
}
