package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.t.TreeOfTales;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamsGrip.class, AlphaMyr.class, TreeOfTales.class})
class DreamsGripTest extends BaseCardTest {

    @Test
    @DisplayName("Tap mode taps the target permanent")
    void tapModeTapsTargetPermanent() {
        Permanent myr = addMyr();
        cast(new int[]{0}, List.of(myr.getId()), false);

        assertThat(myr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untap mode untaps the target permanent")
    void untapModeUntapsTargetPermanent() {
        Permanent myr = addMyr();
        myr.tap();
        cast(new int[]{1}, List.of(myr.getId()), false);

        assertThat(myr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap mode leaves an already-tapped permanent tapped")
    void tapModeLeavesAlreadyTappedPermanentTapped() {
        Permanent myr = addMyr();
        myr.tap();
        cast(new int[]{0}, List.of(myr.getId()), false);

        assertThat(myr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untap mode leaves an already-untapped permanent untapped")
    void untapModeLeavesAlreadyUntappedPermanentUntapped() {
        Permanent myr = addMyr();
        cast(new int[]{1}, List.of(myr.getId()), false);

        assertThat(myr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entwine pays one extra mana and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent first = addMyr();
        Permanent second = addMyr();
        second.tap();
        cast(new int[]{0, 1}, List.of(first.getId(), second.getId()), true);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine allows both modes to target the same permanent")
    void entwineAllowsBothModesToTargetSamePermanent() {
        Permanent myr = addMyr();
        myr.tap();
        cast(new int[]{0, 1}, List.of(myr.getId(), myr.getId()), true);

        assertThat(myr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entwine without the additional mana is rejected")
    void entwineWithoutAdditionalManaIsRejected() {
        Permanent myr = addMyr();
        harness.setHand(player1, List.of(new DreamsGrip()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(myr.getId(), myr.getId())))
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

    @Test
    @DisplayName("Entwine still untaps its second target when the first target leaves")
    void entwineUntapsSurvivingSecondTarget() {
        Permanent first = addMyr();
        Permanent second = addMyr();
        second.tap();
        harness.setHand(player1, List.of(new DreamsGrip()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Dream's Grip");
    }

    @Test
    @DisplayName("Entwine still taps its first target when the second target leaves")
    void entwineTapsSurvivingFirstTarget() {
        Permanent first = addMyr();
        Permanent second = addMyr();
        second.tap();
        harness.setHand(player1, List.of(new DreamsGrip()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Dream's Grip");
    }

    @Test
    @DisplayName("A single mode costs only one blue mana even when extra mana is available")
    void singleModeDoesNotChargeEntwineCost() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.setHand(player1, List.of(new DreamsGrip()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(myr.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(myr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entwine can tap and untap noncreature permanents without affecting other permanents")
    void entwineTargetsArtifactLands() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TreeOfTales());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new TreeOfTales());
        Permanent untouched = addMyr();
        second.tap();

        cast(new int[]{0, 1}, List.of(first.getId(), second.getId()), true);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        assertThat(untouched.isTapped()).isFalse();
    }

    private Permanent addMyr() {
        return harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
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
