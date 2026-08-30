package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplorersCache.class, LlanowarElves.class, Shock.class})
class ExplorersCacheTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new ExplorersCache()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findCache().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains a +1/+1 counter when a controlled creature with one dies")
    void gainsCounterWhenCreatureWithCounterDies() {
        Permanent cache = addReadyCache();
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithShock(elves);

        assertThat(cache.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does not trigger when a controlled creature has no +1/+1 counter")
    void doesNotTriggerForCreatureWithoutCounter() {
        Permanent cache = addReadyCache();
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        killWithShock(elves);

        assertThat(cache.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Moves a +1/+1 counter onto the target creature")
    void movesCounterToTargetCreature() {
        Permanent cache = addReadyCache();
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(cache.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        addReadyCache();
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Activating with no counters does not add one to the target")
    void noCounterDoesNotMoveAnything() {
        Permanent cache = addReadyCache();
        cache.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(cache.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyCache() {
        Permanent cache = harness.addToBattlefieldAndReturn(player1, new ExplorersCache());
        cache.setSummoningSick(false);
        cache.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        return cache;
    }

    private Permanent findCache() {
        return findPermanent(player1, "Explorer's Cache");
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
