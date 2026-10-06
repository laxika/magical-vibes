package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenhillFlock.class})
class RavenhillFlockTest extends BaseCardTest {

    @Test
    @DisplayName("Each card drawn puts a +1/+1 counter on Ravenhill Flock")
    void eachDrawAddsCounter() {
        Permanent flock = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        harness.setLibrary(player1, List.of(new RavenhillFlock(), new RavenhillFlock()));

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Ravenhill Flock")
    void doesNotTriggerOnOpponentDraw() {
        Permanent flock = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        harness.setLibrary(player2, List.of(new RavenhillFlock()));

        draw(player2.getId());
        resolveAllTriggers();

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A draw adds no counter until the trigger resolves")
    void counterWaitsForResolution() {
        Permanent flock = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        harness.setLibrary(player1, List.of(new RavenhillFlock()));

        draw(player1.getId());

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Drawing multiple cards puts a counter per draw on each Flock")
    void multipleCardsGrowEachFlock() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RavenhillFlock());
        harness.setLibrary(player1, List.of(new RavenhillFlock(), new RavenhillFlock(), new RavenhillFlock()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 3));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A pending draw trigger does not put a counter on a new Flock")
    void departedSourceDoesNotGrowNewFlock() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        harness.setLibrary(player1, List.of(new RavenhillFlock()));

        draw(player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        resolveAllTriggers();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }
}
