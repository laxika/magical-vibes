package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GemstoneMine.class})
class GemstoneMineTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three mining counters")
    void entersWithThreeMiningCounters() {
        harness.setHand(player1, List.of(new GemstoneMine()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Gemstone Mine").getCounterCount(CounterType.MINING))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a mining counter and adds the chosen color of mana")
    void removesCounterAndAddsChosenMana() {
        Permanent mine = addReadyMine(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(mine.getCounterCount(CounterType.MINING)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mine.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices immediately when its ability removes the last mining counter")
    void sacrificesImmediatelyAfterLastCounterIsRemoved() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.MINING, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Gemstone Mine");
        harness.assertInGraveyard(player1, "Gemstone Mine");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not sacrifice when another effect removes the last mining counter")
    void doesNotSacrificeWhenAnotherEffectRemovesLastCounter() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.MINING, 1);
        mine.setCounterCount(CounterType.MINING, 0);

        harness.runStateBasedActions();

        assertThat(mine.getCounterCount(CounterType.MINING)).isZero();
        harness.assertOnBattlefield(player1, "Gemstone Mine");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a mining counter to remove")
    void cannotActivateWithoutMiningCounters() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.MINING, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters to remove");

        assertThat(mine.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Gemstone Mine");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Can produce each color immediately after entering the battlefield")
    void producesEachColorImmediatelyAfterEntering(ManaColor color) {
        Permanent mine = harness.enterBattlefieldAndReturn(player1, new GemstoneMine());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(mine.getCounterCount(CounterType.MINING)).isEqualTo(2);
        assertThat(mine.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Gemstone Mine");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three successive uses exhaust the counters and sacrifice the land")
    void threeUsesExhaustCountersAndSacrificeLand() {
        Permanent mine = harness.enterBattlefieldAndReturn(player1, new GemstoneMine());

        for (int use = 1; use <= 3; use++) {
            harness.activateAbility(player1, 0, null, null);
            harness.handleListChoice(player1, "GREEN");

            assertThat(mine.getCounterCount(CounterType.MINING)).isEqualTo(3 - use);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(use);
            assertThat(gd.stack).isEmpty();
            if (use < 3) {
                harness.assertOnBattlefield(player1, "Gemstone Mine");
                harness.assertNotInGraveyard(player1, "Gemstone Mine");
                harness.performUntapStep(player1);
            }
        }

        harness.assertNotOnBattlefield(player1, "Gemstone Mine");
        harness.assertInGraveyard(player1, "Gemstone Mine");
    }

    private Permanent addReadyMine(Player player) {
        Permanent mine = harness.enterBattlefieldAndReturn(player, new GemstoneMine());
        mine.setSummoningSick(false);
        return mine;
    }
}
