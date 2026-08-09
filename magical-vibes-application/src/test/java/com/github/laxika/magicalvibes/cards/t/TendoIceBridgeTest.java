package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyBridge(Player player) {
        Permanent permanent = new Permanent(new TendoIceBridge());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
