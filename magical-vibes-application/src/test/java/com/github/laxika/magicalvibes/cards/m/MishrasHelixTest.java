package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VoltaicKey;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishrasHelix.class, Forest.class, VoltaicKey.class})
class MishrasHelixTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps two target lands")
    void tapsXTargetLands() {
        harness.addToBattlefield(player1, new MishrasHelix());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("More targets than the paid X are rejected")
    void rejectsMoreTargetsThanX() {
        harness.addToBattlefield(player1, new MishrasHelix());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=2 requires exactly two target lands")
    void requiresExactlyXTargets() {
        harness.addToBattlefield(player1, new MishrasHelix());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 taps no land but still taps Mishra's Helix")
    void allowsZeroTargetsWhenXIsZero() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasHelix());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonland permanent is an illegal target")
    void rejectsNonLandTarget() {
        harness.addToBattlefield(player1, new MishrasHelix());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new VoltaicKey());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
