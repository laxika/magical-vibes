package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed(Chronozoa.class)
class ChronozoaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters")
    void entersWithTimeCounters() {
        Permanent chronozoa = castChronozoa();

        assertThat(chronozoa.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes one time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent chronozoa = addCreatureReady(player1, new Chronozoa());
        chronozoa.setCounterCount(CounterType.TIME, 3);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(chronozoa.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates two token copies when it dies without time counters")
    void createsTwoTokenCopiesWithoutTimeCounters() {
        Permanent chronozoa = addCreatureReady(player1, new Chronozoa());
        chronozoa.setCounterCount(CounterType.TIME, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chronozoa));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Chronozoa")).hasSize(2);
        assertThat(findPermanents(player1, "Chronozoa")).allMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCounterCount(CounterType.TIME) == 3);
    }

    @Test
    @DisplayName("Does not create token copies when it dies with time counters")
    void doesNotCreateTokenCopiesWithTimeCounters() {
        Permanent chronozoa = addCreatureReady(player1, new Chronozoa());
        chronozoa.setCounterCount(CounterType.TIME, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chronozoa));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Chronozoa")).isEmpty();
    }

    private Permanent castChronozoa() {
        harness.setHand(player1, List.of(new Chronozoa()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Chronozoa");
    }
}
