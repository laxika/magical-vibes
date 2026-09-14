package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgothianElder.class, ArgothianSwine.class, Forest.class})
class ArgothianElderTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps two target lands")
    void untapsTwoTargetLands() {
        Permanent elder = addCreatureReady(player1, new ArgothianElder());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        first.tap();
        second.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(elder.isTapped()).isTrue();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target untapped lands")
    void canTargetUntappedLands() {
        Permanent elder = addCreatureReady(player1, new ArgothianElder());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(elder.isTapped()).isTrue();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target lands controlled by different players")
    void canTargetLandsControlledByDifferentPlayers() {
        addCreatureReady(player1, new ArgothianElder());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownLand.tap();
        opposingLand.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(ownLand.getId(), opposingLand.getId()));
        harness.passBothPriorities();

        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires exactly two distinct land targets")
    void requiresExactlyTwoDistinctLandTargets() {
        Permanent elder = addCreatureReady(player1, new ArgothianElder());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(elder.isTapped()).isFalse();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(elder.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rejects a non-land target")
    void rejectsNonLandTarget() {
        addCreatureReady(player1, new ArgothianElder());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
