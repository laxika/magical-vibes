package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonLineBreaker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterSymmetristTest extends BaseCardTest {

    @Test
    @DisplayName("A creature with equal power and toughness gains trample when it attacks")
    void equalPowerAndToughnessCreatureGainsTrample() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A creature with unequal power and toughness does not trigger")
    void unequalPowerAndToughnessCreatureDoesNotTrigger() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent loxodon = addCreatureReady(player1, new LoxodonLineBreaker());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, loxodon, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
