package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedgeMaze.class})
class HedgeMazeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new HedgeMaze();
        Card nextCard = new HedgeMaze();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new HedgeMaze()));

        harness.playLand(player1, 0);
        Permanent maze = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(maze.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Surveil may leave the top card in the library")
    void mayKeepTopCard() {
        Card topCard = new HedgeMaze();
        Card nextCard = new HedgeMaze();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new HedgeMaze()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil with an empty library completes without a choice")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HedgeMaze()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Taps for green mana")
    void tapsForGreenMana() {
        tapFor(ManaColor.GREEN);
    }

    @Test
    @DisplayName("Taps for blue mana")
    void tapsForBlueMana() {
        tapFor(ManaColor.BLUE);
    }

    private void tapFor(ManaColor color) {
        Permanent maze = addReadyMaze();

        harness.activateAbility(player1, 0, color == ManaColor.GREEN ? 0 : 1, null, null);

        assertThat(maze.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    private Permanent addReadyMaze() {
        Permanent maze = harness.addToBattlefieldAndReturn(player1, new HedgeMaze());
        maze.setSummoningSick(false);
        return maze;
    }
}
