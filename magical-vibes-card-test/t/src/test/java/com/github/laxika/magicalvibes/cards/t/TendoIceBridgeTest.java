package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TendoIceBridge.class})
class TendoIceBridgeTest extends BaseCardTest {

    @Test
    void entersWithOneChargeCounter() {
        harness.setHand(player1, List.of(new TendoIceBridge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent bridge = findPermanent(player1, "Tendo Ice Bridge");
        assertThat(bridge.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void tapsForColorlessWithoutRemovingChargeCounter() {
        Permanent bridge = addReadyBridge(player1);
        bridge.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(bridge.isTapped()).isTrue();
        assertThat(bridge.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void removesChargeCounterAndAddsChosenColor() {
        Permanent bridge = addReadyBridge(player1);
        bridge.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(bridge.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bridge.isTapped()).isTrue();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotUseAnyColorAbilityWithoutChargeCounter() {
        addReadyBridge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    void canChooseEachOtherColor(ManaColor color) {
        Permanent bridge = addReadyBridge(player1);
        bridge.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(bridge.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(bridge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canUseColoredManaImmediatelyAfterPlayingLand() {
        harness.setHand(player1, List.of(new TendoIceBridge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Tendo Ice Bridge").getCounterCount(CounterType.CHARGE))
                .isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canStillProduceColorlessAfterSpendingLastChargeCounter() {
        Permanent bridge = addReadyBridge(player1);
        bridge.setCounterCount(CounterType.CHARGE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        bridge.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(bridge.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bridge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotUseTappedBridgeAndDoesNotSpendCounterOnFailedActivation() {
        Permanent bridge = addReadyBridge(player1);
        bridge.setCounterCount(CounterType.CHARGE, 1);
        bridge.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bridge.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyBridge(Player player) {
        return addCreatureReady(player, new TendoIceBridge());
    }
}
