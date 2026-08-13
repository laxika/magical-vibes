package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgothianElderTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps two target lands")
    void untapsTwoTargetLands() {
        addReadyElder(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        first.tap();
        second.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target lands controlled by different players")
    void canTargetLandsControlledByDifferentPlayers() {
        addReadyElder(player1);
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
    @DisplayName("Rejects a non-land target")
    void rejectsNonLandTarget() {
        addReadyElder(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyElder(Player player) {
        Permanent elder = harness.addToBattlefieldAndReturn(player, new ArgothianElder());
        elder.setSummoningSick(false);
        return elder;
    }
}
