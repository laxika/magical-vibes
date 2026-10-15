package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.ResoluteReinforcements;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyknightSquire.class, ResoluteReinforcements.class})
class SkyknightSquireTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when another creature you control enters")
    void putsCounterWhenAnotherCreatureEnters() {
        Permanent squire = addCreatureReady(player1, new SkyknightSquire());

        harness.setHand(player1, List.of(new SkyknightSquire()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when Skyknight Squire itself enters")
    void doesNotTriggerOnItsOwnEntry() {
        harness.setHand(player1, List.of(new SkyknightSquire()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent squire = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With three +1/+1 counters, has flying and is a Knight")
    void gainsFlyingAndKnightTypeAtThreeCounters() {
        Permanent squire = addCreatureReady(player1, new SkyknightSquire());

        assertThat(gqs.hasKeyword(gd, squire, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, squire)).doesNotContain(CardSubtype.KNIGHT);

        squire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.hasKeyword(gd, squire, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, squire)).contains(CardSubtype.KNIGHT);

        squire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.hasKeyword(gd, squire, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, squire)).doesNotContain(CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("An opponent's creature entering does not add counters")
    void doesNotTriggerForOpponentCreature() {
        Permanent squire = addCreatureReady(player1, new SkyknightSquire());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SkyknightSquire()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each existing Squire triggers independently, but the entering Squire does not")
    void multipleSquiresTriggerIndependently() {
        Permanent first = addCreatureReady(player1, new SkyknightSquire());
        Permanent second = addCreatureReady(player1, new SkyknightSquire());
        harness.setHand(player1, List.of(new SkyknightSquire()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature and its token each add a counter and activate the threshold")
    void tokensAlsoTriggerAndActivateThreshold() {
        Permanent squire = addCreatureReady(player1, new SkyknightSquire());
        squire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new ResoluteReinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, squire, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, squire))
                .contains(CardSubtype.CAT, CardSubtype.SCOUT, CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("More than three counters still grants flying and preserves original creature types")
    void moreThanThreeCountersPreservesOtherTypes() {
        Permanent squire = addCreatureReady(player1, new SkyknightSquire());
        squire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThat(gqs.hasKeyword(gd, squire, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, squire))
                .contains(CardSubtype.CAT, CardSubtype.SCOUT, CardSubtype.KNIGHT);
    }
}
