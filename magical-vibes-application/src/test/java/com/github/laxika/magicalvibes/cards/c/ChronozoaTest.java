package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.Timecrafting;
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

@CardUsed({Chronozoa.class, Timecrafting.class})
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
    @DisplayName("Sacrifices itself after its last time counter is removed")
    void sacrificesItselfAfterLastTimeCounterIsRemoved() {
        Permanent chronozoa = addCreatureReady(player1, new Chronozoa());
        chronozoa.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Chronozoa")).hasSize(2);
        assertThat(findPermanents(player1, "Chronozoa")).allMatch(permanent ->
                permanent.getCard().isToken()
                && permanent.getCounterCount(CounterType.TIME) == 3);
    }

    @Test
    @DisplayName("Does not remove a time counter during an opponent's upkeep")
    void opponentUpkeepDoesNotRemoveTimeCounter() {
        Permanent chronozoa = addCreatureReady(player1, new Chronozoa());
        chronozoa.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(chronozoa.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not sacrifice itself if it has no time counters when its upkeep ability resolves")
    void upkeepDoesNothingWithoutTimeCounters() {
        Permanent chronozoa = addCreatureReady(player1, new Chronozoa());
        chronozoa.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Chronozoa")).containsExactly(chronozoa);
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

    @Test
    @DisplayName("Removing all time counters with Timecrafting triggers sacrifice and replication")
    void externalRemovalOfLastTimeCounterTriggersSacrifice() {
        Permanent chronozoa = castChronozoa();

        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstantForX(player1, 0, 0, 3, chronozoa.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Chronozoa")).hasSize(2)
                .doesNotContain(chronozoa)
                .allMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCounterCount(CounterType.TIME) == 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chronozoa.getCard());
    }

    @Test
    @DisplayName("Removing fewer than all time counters with Timecrafting does not trigger sacrifice")
    void externalRemovalOfNonlastTimeCounterDoesNotSacrifice() {
        Permanent chronozoa = castChronozoa();

        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstantForX(player1, 0, 0, 2, chronozoa.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Chronozoa")).containsExactly(chronozoa);
        assertThat(chronozoa.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vanishing does not trigger at upkeep without time counters")
    void upkeepWithoutTimeCountersDoesNotPutAbilityOnStack() {
        Permanent chronozoa = addCreatureReady(player1, new Chronozoa());
        chronozoa.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Chronozoa")).containsExactly(chronozoa);
    }

    @Test
    @DisplayName("Token copies inherit vanishing and can produce another generation")
    void tokenCopiesVanishAndReplicate() {
        Permanent chronozoa = castChronozoa();
        chronozoa.setCounterCount(CounterType.TIME, 1);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        List<Permanent> firstGeneration = findPermanents(player1, "Chronozoa");
        assertThat(firstGeneration).hasSize(2);

        for (int upkeep = 0; upkeep < 3; upkeep++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        assertThat(findPermanents(player1, "Chronozoa")).hasSize(4)
                .doesNotContainAnyElementsOf(firstGeneration)
                .allMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCounterCount(CounterType.TIME) == 3);
    }

    private Permanent castChronozoa() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Chronozoa(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Chronozoa");
    }
}
