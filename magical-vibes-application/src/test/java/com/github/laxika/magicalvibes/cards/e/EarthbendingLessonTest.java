package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthbendingLesson.class, Forest.class})
class EarthbendingLessonTest extends BaseCardTest {

    @Test
    void earthbendsTargetLandWithFourCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthbendingLesson()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void cannotTargetLandControlledByOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EarthbendingLesson()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dyingLandReturnsTappedWithoutAnimationOrCounters() {
        Permanent land = earthbendForest();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, land));
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();

        assertReturnedForest(land);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void exiledLandReturnsTappedWithoutAnimationOrCounters() {
        Permanent land = earthbendForest();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, land));
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();

        assertReturnedForest(land);
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    void bouncedLandDoesNotReturn() {
        Permanent land = earthbendForest();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, land));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void earthbendingAgainAddsCountersRatherThanResettingThem() {
        Permanent land = earthbendForest();
        harness.setHand(player1, List.of(new EarthbendingLesson()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);
    }

    private Permanent earthbendForest() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthbendingLesson()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0, land.getId());
        return land;
    }

    private void assertReturnedForest(Permanent original) {
        harness.assertOnBattlefield(player1, "Forest");
        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest"));
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCard().getId()).isEqualTo(original.getCard().getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
