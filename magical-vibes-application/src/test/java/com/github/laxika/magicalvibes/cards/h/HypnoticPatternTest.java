package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HypnoticPattern.class, GrizzlyBears.class, FountainOfYouth.class})
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
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new HypnoticPattern()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    private void castHypnoticPattern(Permanent perpetualTarget, Permanent temporaryTarget) {
        harness.setHand(player1, List.of(new HypnoticPattern()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(perpetualTarget.getId(), temporaryTarget.getId()));
        harness.passBothPriorities();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
