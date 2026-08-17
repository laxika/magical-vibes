package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RealitySpasmTest extends BaseCardTest {

    @Test
    @DisplayName("Tap mode taps exactly X target permanents")
    void tapModeTapsExactlyXTargetPermanents() {
        Permanent first = addPermanent();
        Permanent second = addPermanent();
        Permanent third = addPermanent();

        cast(0, 2, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap mode untaps exactly X target permanents")
    void untapModeUntapsExactlyXTargetPermanents() {
        Permanent first = addPermanent();
        Permanent second = addPermanent();
        first.tap();
        second.tap();

        cast(1, 2, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("X=0 resolves without targets")
    void zeroXResolvesWithoutTargets() {
        Permanent permanent = addPermanent();
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playModalXCard(gd, player1, 0, 0, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(permanent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Fewer than X targets are rejected")
    void fewerThanXTargetsAreRejected() {
        Permanent permanent = addPermanent();
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, 0, 2, null, List.of(permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPermanent() {
        return harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
    }

    private void cast(int mode, int xValue, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 2);
        gs.playModalXCard(gd, player1, 0, mode, xValue, null, targetIds);
        harness.passBothPriorities();
    }
}
