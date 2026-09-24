package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathbreakerIbex.class, GrizzlyBears.class, HillGiant.class})
@DisplayName("Pathbreaker Ibex")
class PathbreakerIbexTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives own creatures trample and a boost based on greatest power")
    void attackBoostsOwnCreaturesByGreatestPower() {
        Permanent ibex = addCreatureReady(player1, new PathbreakerIbex());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ibex.getEffectivePower()).isEqualTo(6);
        assertThat(ibex.getEffectiveToughness()).isEqualTo(6);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(hillGiant.getEffectivePower()).isEqualTo(6);
        assertThat(hillGiant.getEffectiveToughness()).isEqualTo(6);
        assertThat(ibex.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(hillGiant.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(enemy.getEffectivePower()).isEqualTo(2);
        assertThat(enemy.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new PathbreakerIbex());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
