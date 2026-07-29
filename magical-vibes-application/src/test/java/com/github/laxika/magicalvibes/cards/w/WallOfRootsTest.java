package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallOfRootsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a -0/-1 counter on the Wall and adds one green mana")
    void activationAddsGreenManaAndCounter() {
        Permanent wall = addWall(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(wall.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(wall.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can only be activated once each turn")
    void onlyOncePerTurn() {
        Permanent wall = addWall(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wall.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Wall does not need to be untapped or tap to activate its ability")
    void doesNotTapToActivate() {
        Permanent wall = addWall(player1);
        wall.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(wall.isTapped()).isTrue();
    }

    private Permanent addWall(Player player) {
        Permanent wall = new Permanent(new WallOfRoots());
        wall.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(wall);
        return wall;
    }
}
