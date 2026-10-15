package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DanceOfTheSkywise.class, GrizzlyBears.class, SerraAngel.class, FountainOfYouth.class,
        GiantGrowth.class})
class DanceOfTheSkywiseTest extends BaseCardTest {

    @Test
    void transformsTargetCreatureIntoBlueDragonIllusion() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDanceOfTheSkywise(bear);

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasColor(gd, bear, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.DRAGON)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.ILLUSION)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.BEAR)).isFalse();
    }

    @Test
    void losesExistingAbilitiesButGainsFlying() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();

        castDanceOfTheSkywise(angel);

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void transformationWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDanceOfTheSkywise(bear);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasColor(gd, bear, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.BEAR)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.DRAGON)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.ILLUSION)).isFalse();
    }

    @Test
    void cannotTargetCreatureOpponentControls() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DanceOfTheSkywise()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DanceOfTheSkywise()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void countersStillModifyTheNewBasePowerAndToughness() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castDanceOfTheSkywise(bear);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.hasColor(gd, bear, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, bear, CardColor.GREEN)).isFalse();
    }

    @Test
    void earlierPowerAndToughnessBoostStillApplies() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        castDanceOfTheSkywise(bear);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(7);
    }

    @Test
    void laterPowerAndToughnessBoostStillApplies() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDanceOfTheSkywise(bear);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    void printedAbilitiesReturnAfterCleanup() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        castDanceOfTheSkywise(angel);
        harness.forceStep(TurnStep.END_STEP);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.BLUE)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(angel, CardSubtype.ANGEL)).isTrue();
    }

    @Test
    void doesNotTransformOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        castDanceOfTheSkywise(target);

        assertThat(gqs.hasColor(gd, other, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(other, CardSubtype.DRAGON)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(other, CardSubtype.ANGEL)).isTrue();
    }

    private void castDanceOfTheSkywise(Permanent target) {
        harness.setHand(player1, List.of(new DanceOfTheSkywise()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
