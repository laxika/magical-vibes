package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathbreakerIbex.class, GrizzlyBears.class, HillGiant.class, GiantGrowth.class})
class PathbreakerIbexTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts and grants trample to all creatures you control")
    void attackBoostsOwnCreaturesAndGrantsTrample() {
        Permanent ibex = addCreatureReady(player1, new PathbreakerIbex());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ibex)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ibex)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hillGiant)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ibex, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hillGiant, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack boost and trample wear off at end of turn")
    void attackEffectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new PathbreakerIbex());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Greatest power is evaluated when the attack trigger resolves")
    void usesPowerAfterAResponseResolves() {
        Permanent ibex = addCreatureReady(player1, new PathbreakerIbex());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ibex)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ibex)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Each Ibex trigger uses the greatest power after earlier triggers resolve")
    void multipleIbexTriggersCompoundTheBoost() {
        Permanent first = addCreatureReady(player1, new PathbreakerIbex());
        Permanent second = addCreatureReady(player1, new PathbreakerIbex());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(12);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(12);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(11);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost or trample")
    void lateCreatureDoesNotReceiveResolvedEffects() {
        Permanent ibex = addCreatureReady(player1, new PathbreakerIbex());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ibex)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger a nonattacking Ibex")
    void doesNotTriggerForAnotherAttacker() {
        Permanent ibex = addCreatureReady(player1, new PathbreakerIbex());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ibex)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ibex, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
