package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Colossadactyl;
import com.github.laxika.magicalvibes.cards.r.RelicsRoar;
import com.github.laxika.magicalvibes.cards.r.RiverHeraldScout;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PugnaciousHammerskull.class, Colossadactyl.class, RiverHeraldScout.class, RelicsRoar.class})
class PugnaciousHammerskullTest extends BaseCardTest {

    @Test
    void putsStunCounterOnItselfWhenAttackingWithoutAnotherDinosaur() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void doesNotPutStunCounterOnItselfWhenControllingAnotherDinosaur() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());
        addCreatureReady(player1, new Colossadactyl());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void anotherNonDinosaurDoesNotPreventTheStunCounter() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());
        addCreatureReady(player1, new RiverHeraldScout());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void opponentDinosaurDoesNotPreventTheStunCounter() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());
        addCreatureReady(player2, new Colossadactyl());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void gainingAnotherDinosaurInResponseDoesNotPreventTheStunCounter() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());
        Permanent scout = addCreatureReady(player1, new RiverHeraldScout());
        harness.setHand(player1, List.of(new RelicsRoar()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.stack).hasSize(1);

            harness.castInstant(player1, 0, scout.getId());
            resolveAllTriggers();

            assertThat(hammerskull.getCounterCount(CounterType.STUN)).isEqualTo(1);
        });
    }

    @Test
    void doesNotTriggerAtAllWhenAnotherDinosaurIsControlled() {
        addCreatureReady(player1, new PugnaciousHammerskull());
        addCreatureReady(player1, new Colossadactyl());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));

            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    void twoAttackingHammerskullsPreventEachOthersStunCounters() {
        Permanent first = addCreatureReady(player1, new PugnaciousHammerskull());
        Permanent second = addCreatureReady(player1, new PugnaciousHammerskull());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.STUN)).isZero();
        assertThat(second.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void stunCounterReplacesTheNextUntapAndThenAllowsUntapping() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();

            assertThat(hammerskull.isTapped()).isTrue();
            assertThat(hammerskull.getCounterCount(CounterType.STUN)).isEqualTo(1);
        });

        harness.performUntapStep(player1);

        assertThat(hammerskull.isTapped()).isTrue();
        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player1);

        assertThat(hammerskull.isTapped()).isFalse();
    }
}
