package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathbreakerIbex.class, GrizzlyBears.class, HillGiant.class})
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
}
