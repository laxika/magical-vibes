package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HighStride.class, GrizzlyBears.class, FountainOfYouth.class})
class HighStrideTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and boosts a creature with reach")
    void untapsAndBoostsTargetCreatureWithReach() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        castHighStride(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Boost and reach wear off at end of turn")
    void boostAndReachWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castHighStride(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new HighStride()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An untapped creature you control still gets the boost and reach")
    void boostsUntappedCreatureYouControlOnly() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.tap();

        castHighStride(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Does not affect another creature when its target leaves before resolution")
    void targetLeavingBeforeResolutionDoesNotRedirectEffects() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        other.tap();
        harness.setHand(player1, List.of(new HighStride()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "High Stride");
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.REACH)).isFalse();
    }

    private void castHighStride(Permanent target) {
        harness.setHand(player1, List.of(new HighStride()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
