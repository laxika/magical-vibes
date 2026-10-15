package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PillardropWarden;
import com.github.laxika.magicalvibes.cards.s.ScurridColony;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterSymmetrist.class, ScurridColony.class, PillardropWarden.class})
class MasterSymmetristTest extends BaseCardTest {

    @Test
    @DisplayName("A creature with equal power and toughness gains trample when it attacks")
    void equalPowerAndToughnessCreatureGainsTrample() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent bears = addCreatureReady(player1, new ScurridColony());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A creature with unequal power and toughness does not trigger")
    void unequalPowerAndToughnessCreatureDoesNotTrigger() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent loxodon = addCreatureReady(player1, new PillardropWarden());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, loxodon, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent bears = addCreatureReady(player1, new ScurridColony());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void masterSymmetristGrantsItselfTrampleWhenItAttacks() {
        Permanent master = addCreatureReady(player1, new MasterSymmetrist());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, master, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void eachQualifyingAttackerGainsTrampleButNonattackersDoNot() {
        Permanent master = addCreatureReady(player1, new MasterSymmetrist());
        Permanent first = addCreatureReady(player1, new ScurridColony());
        Permanent second = addCreatureReady(player1, new ScurridColony());
        Permanent unequal = addCreatureReady(player1, new PillardropWarden());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unequal, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, master, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentsAttackerDoesNotGainTrample() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent attacker = addCreatureReady(player2, new ScurridColony());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void unequalEffectiveStatsPreventTheTrigger() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent attacker = addCreatureReady(player1, new ScurridColony());
        attacker.setPowerModifier(1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void equalEffectiveStatsTriggerEvenWhenPrintedStatsAreUnequal() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent attacker = addCreatureReady(player1, new PillardropWarden());
        attacker.setPowerModifier(4);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void changingStatsAfterTriggeringDoesNotPreventTrample() {
        addCreatureReady(player1, new MasterSymmetrist());
        Permanent attacker = addCreatureReady(player1, new ScurridColony());

        declareAttackers(player1, List.of(1));
        attacker.setPowerModifier(1);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }
}
