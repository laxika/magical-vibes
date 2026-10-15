package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceTunnel.class})
class IceTunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new IceTunnel()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Ice Tunnel").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingForBlueMana() {
        tapFor(ManaColor.BLUE);
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingForBlackMana() {
        tapFor(ManaColor.BLACK);
    }

    @Test
    @DisplayName("Enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new IceTunnel());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot produce mana while tapped, but can after the untap step")
    void producesManaAfterUntapping() {
        harness.setHand(player1, List.of(new IceTunnel()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.performUntapStep(player1);
        assertThat(findPermanent(player1, "Ice Tunnel").isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Ice Tunnel").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void tapFor(ManaColor color) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new IceTunnel());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(color)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
