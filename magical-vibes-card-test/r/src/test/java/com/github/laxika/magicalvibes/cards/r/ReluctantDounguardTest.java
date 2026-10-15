package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReluctantDounguard.class, GrizzlyBears.class})
class ReluctantDounguardTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two -1/-1 counters")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ReluctantDounguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent dounguard = findPermanent(player1, "Reluctant Dounguard");

        assertThat(dounguard.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another creature entering under its controller's control removes a counter")
    void allyCreatureEnteringRemovesCounter() {
        Permanent dounguard = addDounguardWithCounters(2);

        castGrizzlyBears(player1);
        resolveAllTriggers();

        assertThat(dounguard.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when it has no -1/-1 counters")
    void doesNotTriggerWithoutCounters() {
        Permanent dounguard = addDounguardWithCounters(0);

        castGrizzlyBears(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(dounguard.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger does nothing if its last counter is removed before resolution")
    void triggerDoesNothingAfterCounterIsRemoved() {
        Permanent dounguard = addDounguardWithCounters(1);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        dounguard.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(dounguard.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature entering does not remove a counter")
    void opponentCreatureEnteringDoesNotTrigger() {
        Permanent dounguard = addDounguardWithCounters(1);

        castGrizzlyBears(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(dounguard.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering does not trigger its own counter removal")
    void enteringDoesNotRemoveItsOwnCounter() {
        castDounguard();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Reluctant Dounguard")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A second Dounguard removes only the older Dounguard's last counter")
    void anotherDounguardRemovesLastCounter() {
        Permanent older = addDounguardWithCounters(1);

        castDounguard();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        List<Permanent> dounguards = findPermanents(player1, "Reluctant Dounguard");
        Permanent newer = dounguards.stream().filter(p -> p != older).findFirst().orElseThrow();
        assertThat(newer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        resolveAllTriggers();

        assertThat(older.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(newer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adding a counter after another creature entered does not create a trigger")
    void counterAddedAfterEntryDoesNotTrigger() {
        Permanent older = addDounguardWithCounters(0);

        castDounguard();
        harness.passBothPriorities();
        older.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(older.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A pending trigger cannot remove a counter from a different Dounguard")
    void departedSourceDoesNotRemoveCounterFromAnotherCopy() {
        Permanent older = addDounguardWithCounters(1);

        castDounguard();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent newer = findPermanents(player1, "Reluctant Dounguard").stream()
                .filter(p -> p != older).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(older);
        gd.playerGraveyards.get(player1.getId()).add(older.getCard());
        resolveAllTriggers();

        assertThat(newer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    private void castDounguard() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ReluctantDounguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private Permanent addDounguardWithCounters(int count) {
        Permanent dounguard = harness.addToBattlefieldAndReturn(player1, new ReluctantDounguard());
        dounguard.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, count);
        return dounguard;
    }

    private void castGrizzlyBears(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
