package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DownwindAmbusher.class, GiantSpider.class, GrizzlyBears.class, Shock.class})
class DownwindAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 gives an opposing creature -1/-1 until end of turn")
    void givesOpposingCreatureMinusOneMinusOne() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 1 destroys an opposing creature dealt damage this turn")
    void destroysDamagedOpposingCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, spider.getId());
        harness.passBothPriorities();

        cast(1, spider.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Mode 1 cannot target an opposing creature that was not dealt damage this turn")
    void modeOneRequiresDamageThisTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither mode can target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> cast(0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new DownwindAmbusher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, mode, targetId);
    }
}
