package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.OminousParcel;
import com.github.laxika.magicalvibes.cards.w.WreckingCrew;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevelationOfPower.class, WreckingCrew.class, OminousParcel.class})
class RevelationOfPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature without granting keywords when it has no counters")
    void boostsCreatureWithoutCounter() {
        Permanent target = addCreatureReady(player1, new WreckingCrew());
        castRevelation(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Grants flying and lifelink to a target creature with any counter")
    void grantsKeywordsToCreatureWithCounter() {
        Permanent target = addCreatureReady(player1, new WreckingCrew());
        target.setCounterCount(CounterType.CHARGE, 1);
        castRevelation(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The boost and granted keywords wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent target = addCreatureReady(player1, new WreckingCrew());
        target.setCounterCount(CounterType.CHARGE, 1);
        castRevelation(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new OminousParcel());
        harness.setHand(player1, List.of(new RevelationOfPower()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        var targetId = harness.getPermanentId(player1, "Ominous Parcel");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Checks counters added after casting at resolution")
    void counterAddedBeforeResolutionGrantsKeywords() {
        Permanent target = addCreatureReady(player1, new WreckingCrew());
        harness.setHand(player1, List.of(new RevelationOfPower()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());

        target.setCounterCount(CounterType.SHIELD, 1);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Still boosts the creature when its last counter is removed before resolution")
    void counterRemovedBeforeResolutionPreventsOnlyKeywords() {
        Permanent target = addCreatureReady(player1, new WreckingCrew());
        target.setCounterCount(CounterType.SHIELD, 1);
        harness.setHand(player1, List.of(new RevelationOfPower()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());

        target.setCounterCount(CounterType.SHIELD, 0);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Removing the counter after resolution does not remove the granted abilities")
    void keywordsPersistAfterCounterIsRemoved() {
        Permanent target = addCreatureReady(player1, new WreckingCrew());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castRevelation(target);

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Adding a counter after resolution does not grant abilities retroactively")
    void counterAddedAfterResolutionDoesNotGrantKeywords() {
        Permanent target = addCreatureReady(player1, new WreckingCrew());
        castRevelation(target);

        target.setCounterCount(CounterType.SHIELD, 1);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Can boost and grant abilities to an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new WreckingCrew());
        target.setCounterCount(CounterType.SHIELD, 1);
        castRevelation(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    private void castRevelation(Permanent target) {
        harness.setHand(player1, List.of(new RevelationOfPower()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
