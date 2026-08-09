package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreamsGripTest extends BaseCardTest {

    @Test
    @DisplayName("Tap mode taps the target permanent")
    void tapModeTapsTargetPermanent() {
        Permanent tome = addTome();
        cast(new int[]{0}, List.of(tome.getId()), false);

        assertThat(tome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untap mode untaps the target permanent")
    void untapModeUntapsTargetPermanent() {
        Permanent tome = addTome();
        tome.tap();
        cast(new int[]{1}, List.of(tome.getId()), false);

        assertThat(tome.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entwine pays one extra mana and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent first = addTome();
        Permanent second = addTome();
        second.tap();
        cast(new int[]{0, 1}, List.of(first.getId(), second.getId()), true);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine without the additional mana is rejected")
    void entwineWithoutAdditionalManaIsRejected() {
        Permanent tome = addTome();
        harness.setHand(player1, List.of(new DreamsGrip()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(tome.getId(), tome.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A non-permanent target is rejected")
    void nonPermanentTargetIsRejected() {
        harness.setHand(player1, List.of(new DreamsGrip()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTome() {
        return harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, boolean entwined) {
        harness.setHand(player1, List.of(new DreamsGrip()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        if (entwined) {
            harness.addMana(player1, ManaColor.COLORLESS, 1);
        }
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
