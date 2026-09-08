package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NimbusMaze.class, Island.class, Plains.class})
class NimbusMazeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana produces one colorless")
    void tappingForColorlessMana() {
        Permanent maze = addReadyMaze(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(maze.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White mana ability requires an Island")
    void whiteManaRequiresIsland() {
        Permanent maze = addReadyMaze(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an Island");
        assertThat(maze.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for white mana works while controlling an Island")
    void tappingForWhiteManaWithIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent maze = addReadyMaze(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(maze.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blue mana ability requires a Plains")
    void blueManaRequiresPlains() {
        Permanent maze = addReadyMaze(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a Plains");
        assertThat(maze.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for blue mana works while controlling a Plains")
    void tappingForBlueManaWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        Permanent maze = addReadyMaze(player1);

        harness.activateAbility(player1, 1, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(maze.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Island does not enable white mana")
    void opponentsIslandDoesNotEnableWhiteMana() {
        harness.addToBattlefield(player2, new Island());
        Permanent maze = addReadyMaze(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(maze.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Plains does not enable blue mana")
    void opponentsPlainsDoesNotEnableBlueMana() {
        harness.addToBattlefield(player2, new Plains());
        Permanent maze = addReadyMaze(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(maze.isTapped()).isFalse();
    }

    private Permanent addReadyMaze(Player player) {
        Permanent maze = new Permanent(new NimbusMaze());
        maze.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(maze);
        return maze;
    }
}
