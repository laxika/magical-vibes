package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({SunbringersTouch.class, GrizzlyBears.class, HillGiant.class, GiantSpider.class})
@DisplayName("Sunbringer's Touch")
class SunbringersTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Bolsters by hand size and grants trample to creatures with +1/+1 counters")
    void bolstersByHandSizeAndGrantsTrampleToCounterBearers() {
        Permanent leastToughCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent alreadyModifiedCreature = addCreatureReady(player1, new HillGiant());
        alreadyModifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutCounter = addCreatureReady(player1, new GiantSpider());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(
                new SunbringersTouch(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(alreadyModifiedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(leastToughCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(alreadyModifiedCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(withoutCounter.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample lasts until end of turn")
    void trampleExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new SunbringersTouch()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void resumesTrampleGrantAfterChoosingAmongTiedCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent counterBearer = addCreatureReady(player1, new HillGiant());
        counterBearer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new SunbringersTouch(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(counterBearer.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(counterBearer.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void usesHandSizeAndEffectiveToughnessAtResolution() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent giant = addCreatureReady(player1, new HillGiant());

        harness.setHand(player1, List.of(new SunbringersTouch(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(giant.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void emptyHandDoesNotAddCountersOrGrantTrampleToUnmodifiedCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SunbringersTouch()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void resolvesWithoutCreaturesAndDoesNotAffectLaterCreatures() {
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new SunbringersTouch(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.hasKeyword(Keyword.TRAMPLE)).isFalse();
        Permanent laterCreature = addCreatureReady(player1, new GrizzlyBears());
        laterCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(laterCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
