package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MutationalAdvantage.class, GrizzlyBears.class, Pyroclasm.class})
class MutationalAdvantageTest extends BaseCardTest {

    @Test
    @DisplayName("Grants keywords to your permanents with counters and proliferates")
    void grantsKeywordsAndProliferates() {
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.CHARGE, 1);
        Permanent withoutCounters = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCountered = addCreatureReady(player2, new GrizzlyBears());
        opponentCountered.setCounterCount(CounterType.CHARGE, 1);

        castMutationalAdvantage(countered);

        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, withoutCounters, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, withoutCounters, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCountered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCountered, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(countered.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        Permanent laterCountered = addCreatureReady(player1, new GrizzlyBears());
        laterCountered.setCounterCount(CounterType.CHARGE, 1);
        assertThat(gqs.hasKeyword(gd, laterCountered, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Prevents all damage to your permanents with counters")
    void preventsDamageToCounteredPermanents() {
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.CHARGE, 1);
        Permanent withoutCounters = addCreatureReady(player1, new GrizzlyBears());

        castMutationalAdvantage();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Pyroclasm()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(countered);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(withoutCounters);
        assertThat(countered.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The temporary keyword grants wear off at end of turn")
    void keywordGrantsWearOffAtEndOfTurn() {
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.CHARGE, 1);

        castMutationalAdvantage();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castMutationalAdvantage(Permanent... proliferateTargets) {
        harness.setHand(player1, List.of(new MutationalAdvantage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(proliferateTargets).stream().map(Permanent::getId).toList());
    }
}
