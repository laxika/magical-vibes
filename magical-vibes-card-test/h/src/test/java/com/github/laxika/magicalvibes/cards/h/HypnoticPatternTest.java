package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Blur;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WildShape;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HypnoticPattern.class, GrizzlyBears.class, FountainOfYouth.class, Blur.class, WildShape.class})
class HypnoticPatternTest extends BaseCardTest {

    @Test
    void appliesPerpetualAndTemporaryPowerReductionToSeparateTargets() {
        Permanent perpetualTarget = addCreature(player2);
        Permanent temporaryTarget = addCreature(player2);

        castHypnoticPattern(perpetualTarget, temporaryTarget);

        assertThat(gqs.getEffectivePower(gd, perpetualTarget)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, perpetualTarget)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, temporaryTarget)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, temporaryTarget)).isEqualTo(2);
    }

    @Test
    void temporaryReductionWearsOffButPerpetualReductionRemains() {
        Permanent perpetualTarget = addCreature(player2);
        Permanent temporaryTarget = addCreature(player2);

        castHypnoticPattern(perpetualTarget, temporaryTarget);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, perpetualTarget)).isZero();
        assertThat(gqs.getEffectivePower(gd, temporaryTarget)).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent creature = addCreature(player2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new HypnoticPattern()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    @Test
    void canChooseTheSameCreatureForBothTargets() {
        Permanent target = addCreature(player2);

        castHypnoticPattern(target, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
    }

    @Test
    void perpetualReductionAppliesAfterSettingBasePowerAndToughness() {
        Permanent perpetualTarget = addCreature(player1);
        Permanent temporaryTarget = addCreature(player2);
        castHypnoticPattern(perpetualTarget, temporaryTarget);

        harness.setHand(player1, List.of(new WildShape()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalInstant(player1, 0, 2, List.of(perpetualTarget.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, perpetualTarget)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perpetualTarget)).isEqualTo(3);
    }

    @Test
    void perpetualReductionSurvivesFlickeringButTemporaryReductionDoesNot() {
        Permanent perpetualTarget = addCreature(player1);
        Permanent temporaryTarget = addCreature(player1);
        castHypnoticPattern(perpetualTarget, temporaryTarget);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        flicker(perpetualTarget);
        Permanent returnedPerpetualTarget = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, returnedPerpetualTarget)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, returnedPerpetualTarget)).isEqualTo(2);

        flicker(temporaryTarget);
        Permanent returnedTemporaryTarget = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, returnedTemporaryTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returnedTemporaryTarget)).isEqualTo(2);
    }

    @Test
    void temporaryTargetStillResolvesWhenPerpetualTargetLeaves() {
        Permanent perpetualTarget = addCreature(player1);
        Permanent temporaryTarget = addCreature(player2);
        harness.setHand(player1, List.of(new HypnoticPattern()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(perpetualTarget.getId(), temporaryTarget.getId()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        flicker(perpetualTarget);
        Permanent returnedTarget = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, returnedTarget)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, temporaryTarget)).isZero();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, temporaryTarget)).isEqualTo(2);
    }

    @Test
    void perpetualTargetStillResolvesWhenTemporaryTargetLeaves() {
        Permanent perpetualTarget = addCreature(player2);
        Permanent temporaryTarget = addCreature(player1);
        harness.setHand(player1, List.of(new HypnoticPattern()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(perpetualTarget.getId(), temporaryTarget.getId()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        flicker(temporaryTarget);
        Permanent returnedTarget = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, returnedTarget)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, perpetualTarget)).isZero();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, perpetualTarget)).isZero();
    }

    private void flicker(Permanent target) {
        harness.setHand(player1, List.of(new Blur()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void castHypnoticPattern(Permanent perpetualTarget, Permanent temporaryTarget) {
        harness.setHand(player1, List.of(new HypnoticPattern()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, List.of(perpetualTarget.getId(), temporaryTarget.getId()));
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
