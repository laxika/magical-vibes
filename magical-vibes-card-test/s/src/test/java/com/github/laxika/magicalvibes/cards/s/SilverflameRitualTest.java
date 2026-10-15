package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TuinvaleTreefolk;
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

@CardUsed({SilverflameRitual.class, GrizzlyBears.class, TuinvaleTreefolk.class})
class SilverflameRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control without Adamant")
    void putsCountersWithoutAdamant() {
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        castWithMana(1, 3);

        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Adamant grants vigilance after putting counters on your creatures")
    void adamantGrantsVigilance() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castWithMana(4, 0);

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Exactly three white mana satisfies adamant for all your creatures only")
    void exactlyThreeWhiteManaGrantsVigilanceOnlyToControlledCreatures() {
        Permanent first = addCreatureReady(player1, new TuinvaleTreefolk());
        Permanent second = addCreatureReady(player1, new TuinvaleTreefolk());
        Permanent opponent = addCreatureReady(player2, new TuinvaleTreefolk());

        castWithMana(3, 1);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Two white mana does not satisfy adamant")
    void twoWhiteManaDoesNotGrantVigilance() {
        Permanent creature = addCreatureReady(player1, new TuinvaleTreefolk());

        castWithMana(2, 2);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither counters nor vigilance")
    void laterCreaturesAreNotAffected() {
        Permanent original = addCreatureReady(player1, new TuinvaleTreefolk());

        castWithMana(3, 1);
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new TuinvaleTreefolk());

        assertThat(gqs.hasKeyword(gd, original, Keyword.VIGILANCE)).isTrue();
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance expires at end of turn while the counter remains")
    void vigilanceExpiresButCounterRemains() {
        Permanent creature = addCreatureReady(player1, new TuinvaleTreefolk());

        castWithMana(3, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    private void castWithMana(int whiteMana, int colorlessMana) {
        harness.setHand(player1, List.of(new SilverflameRitual()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
