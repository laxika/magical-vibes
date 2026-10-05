package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomentOfTriumph.class, RaptorCompanion.class, TravelersAmulet.class, MomentOfCraving.class})
class MomentOfTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target creature and gains 2 life")
    void boostsTargetAndGainsLife() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new MomentOfTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Moment of Triumph");
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new MomentOfTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new TravelersAmulet()).getId();
        harness.setHand(player1, List.of(new MomentOfTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opposing creature while only the caster gains life")
    void boostsOpposingCreatureAndGainsLifeForCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new MomentOfTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not gain life when its only target leaves before resolution")
    void doesNotGainLifeWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new MomentOfTriumph()));
        harness.setHand(player2, List.of(new MomentOfCraving()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raptor Companion");
        harness.assertLife(player2, 22);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Moment of Triumph");
    }
}
