package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HedgeMaze.class, GrizzlyBears.class})
class HedgeMazeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new HedgeMaze()));

        harness.playLand(player1, 0);
        Permanent maze = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(maze.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
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
        Permanent maze = new Permanent(new HedgeMaze());
        maze.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(maze);
        return maze;
    }
}
