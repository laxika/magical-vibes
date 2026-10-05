package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IvorytuskFortress.class, AlpineGrizzly.class, Forest.class})
class IvorytuskFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps creatures with +1/+1 counters during an opponent's untap step")
    void untapsCreaturesWithPlusOneCountersDuringOpponentsUntapStep() {
        Permanent fortress = addCreatureReady(player1, new IvorytuskFortress());
        Permanent counteredCreature = addCreatureReady(player1, new AlpineGrizzly());
        Permanent ageCounterCreature = addCreatureReady(player1, new AlpineGrizzly());
        Permanent uncounteredCreature = addCreatureReady(player1, new AlpineGrizzly());

        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ageCounterCreature.setCounterCount(CounterType.AGE, 1);
        fortress.tap();
        counteredCreature.tap();
        ageCounterCreature.tap();
        uncounteredCreature.tap();

        harness.performUntapStep(player2);

        assertThat(fortress.isTapped()).isTrue();
        assertThat(counteredCreature.isTapped()).isFalse();
        assertThat(ageCounterCreature.isTapped()).isTrue();
        assertThat(uncounteredCreature.isTapped()).isTrue();
    }

    @Test
    void untapsItselfWithACounterButNotCounteredNoncreatures() {
        Permanent fortress = addCreatureReady(player1, new IvorytuskFortress());
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(land);
        fortress.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        fortress.tap();
        land.tap();

        harness.performUntapStep(player2);

        assertThat(fortress.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void stopsUntappingCreaturesAfterLeavingBattlefield() {
        Permanent fortress = addCreatureReady(player1, new IvorytuskFortress());
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.tap();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(fortress);
        gd.playerGraveyards.get(player1.getId()).add(fortress.getCard());
        creature.tap();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void opponentsFortressDoesNotUntapYourCreatures() {
        addCreatureReady(player2, new IvorytuskFortress());
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.tap();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void checksCountersAtEachUntapStep() {
        addCreatureReady(player1, new IvorytuskFortress());
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        creature.tap();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void normalOwnUntapDoesNotRequireCounters() {
        Permanent fortress = addCreatureReady(player1, new IvorytuskFortress());
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        fortress.tap();
        creature.tap();

        harness.performUntapStep(player1);

        assertThat(fortress.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }
}
