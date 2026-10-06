package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealitySpasm.class, NestInvader.class, Island.class})
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
        cast(0, 0, List.of());

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

    @Test
    void tapModeCanTargetOwnLandAndOpposingCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent creature = addPermanent();
        cast(0, 2, List.of(land.getId(), creature.getId()));
        assertThat(land.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void untapModeLeavesUnselectedPermanentsTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent creature = addPermanent();
        Permanent unselected = addPermanent();
        land.tap();
        creature.tap();
        unselected.tap();
        cast(1, 2, List.of(land.getId(), creature.getId()));
        assertThat(land.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(unselected.isTapped()).isTrue();
    }

    @Test
    void duplicateTargetsAreRejected() {
        Permanent permanent = addPermanent();
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        assertThatThrownBy(() -> gs.playModalXCard(gd, player1, 0, 0, 2, null,
                List.of(permanent.getId(), permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapModeSupportsMoreThanOneHundredTargets() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 101)
                .mapToObj(i -> addPermanent()).toList();
        cast(0, 101, targets.stream().map(Permanent::getId).toList());
        assertThat(targets).allMatch(Permanent::isTapped);
    }

    @Test
    void untapModeSupportsMoreThanOneHundredTargets() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 101)
                .mapToObj(i -> addPermanent()).toList();
        targets.forEach(Permanent::tap);
        cast(1, 101, targets.stream().map(Permanent::getId).toList());
        assertThat(targets).noneMatch(Permanent::isTapped);
    }

    @Test
    void xAboveOneHundredStillRequiresExactlyXTargets() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> addPermanent()).toList();
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, 103);
        assertThatThrownBy(() -> gs.playModalXCard(gd, player1, 0, 0, 101, null,
                targets.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapModeWithZeroXDoesNotUntapAnything() {
        Permanent permanent = addPermanent();
        permanent.tap();
        cast(1, 0, List.of());
        assertThat(permanent.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Reality Spasm");
    }

    @Test
    void moreThanXTargetsAreRejected() {
        Permanent first = addPermanent();
        Permanent second = addPermanent();
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        assertThatThrownBy(() -> gs.playModalXCard(gd, player1, 0, 1, 1, null,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainingLegalTargetIsTappedWhenAnotherTargetLeaves() {
        Permanent first = addPermanent();
        Permanent second = addPermanent();
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        gs.playModalXCard(gd, player1, 0, 0, 2, null,
                List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();
        assertThat(second.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Reality Spasm");
    }

    @Test
    void remainingLegalTargetIsUntappedWhenAnotherTargetLeaves() {
        Permanent first = addPermanent();
        Permanent second = addPermanent();
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        gs.playModalXCard(gd, player1, 0, 1, 2, null,
                List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();
        assertThat(second.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Reality Spasm");
    }

    private Permanent addPermanent() {
        return harness.addToBattlefieldAndReturn(player2, new NestInvader());
    }

    private void cast(int mode, int xValue, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new RealitySpasm()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 2);
        gs.playModalXCard(gd, player1, 0, mode, xValue, null, targetIds);
        harness.passBothPriorities();
    }
}
