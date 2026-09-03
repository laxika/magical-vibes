package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WallOfRoots.class)
class WallOfRootsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a -0/-1 counter on the Wall and adds one green mana")
    void activationAddsGreenManaAndCounter() {
        Permanent wall = addCreatureReady(player1, new WallOfRoots());
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
        Permanent wall = addCreatureReady(player1, new WallOfRoots());
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
        Permanent wall = addCreatureReady(player1, new WallOfRoots());
        wall.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The once-per-turn restriction resets on the next turn")
    void oncePerTurnResetsOnNextTurn() {
        Permanent wall = addCreatureReady(player1, new WallOfRoots());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(wall.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The final counter still produces mana before zero toughness removes the Wall")
    void finalActivationProducesManaBeforeWallDies() {
        Permanent wall = addCreatureReady(player1, new WallOfRoots());
        wall.setCounterCount(CounterType.MINUS_ZERO_MINUS_ONE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wall);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wall.getCard());
    }

    @Test
    @CardUsed(Solemnity.class)
    @DisplayName("The counter cost cannot be paid while Solemnity prevents counters")
    void cannotActivateWhenCountersCannotBePlaced() {
        Permanent wall = addCreatureReady(player1, new WallOfRoots());
        harness.addToBattlefield(player1, new Solemnity());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wall.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
