package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CandelabraOfTawnos.class, Forest.class, GrizzlyBears.class})
class CandelabraOfTawnosTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps X target lands")
    void untapsXTargetLands() {
        Permanent candelabra = harness.addToBattlefieldAndReturn(player1, new CandelabraOfTawnos());
        candelabra.setSummoningSick(false);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        first.tap();
        second.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rejects more targets than X")
    void rejectsMoreTargetsThanX() {
        harness.addToBattlefield(player1, new CandelabraOfTawnos());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a non-land target")
    void rejectsNonLandTarget() {
        harness.addToBattlefield(player1, new CandelabraOfTawnos());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsFewerTargetsThanX() {
        harness.addToBattlefield(player1, new CandelabraOfTawnos());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoTargetsWhenXIsPositive() {
        harness.addToBattlefield(player1, new CandelabraOfTawnos());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allowsZeroXAndStillPaysTapCost() {
        Permanent candelabra = harness.addToBattlefieldAndReturn(player1, new CandelabraOfTawnos());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());

        assertThat(candelabra.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsDuplicateLandTargets() {
        harness.addToBattlefield(player1, new CandelabraOfTawnos());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newlyEnteredArtifactCanActivateAndCannotActivateAgainWhileTapped() {
        Permanent candelabra = harness.addToBattlefieldAndReturn(player1, new CandelabraOfTawnos());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(land.getId()));

        assertThat(candelabra.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(land.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsActivationWithoutEnoughMana() {
        harness.addToBattlefield(player1, new CandelabraOfTawnos());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapsRemainingTargetWhenAnotherTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new CandelabraOfTawnos());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        first.tap();
        second.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void allowsMoreThanOneHundredTargetsWhenXMatches() {
        harness.addToBattlefield(player1, new CandelabraOfTawnos());
        List<Permanent> lands = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Forest()))
                .toList();
        lands.forEach(Permanent::tap);
        harness.addMana(player1, ManaColor.COLORLESS, 101);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 101,
                lands.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(lands).allSatisfy(land -> assertThat(land.isTapped()).isFalse());
    }
}
