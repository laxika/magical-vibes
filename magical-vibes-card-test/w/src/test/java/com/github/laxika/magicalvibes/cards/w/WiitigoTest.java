package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wiitigo.class, BalduvianBears.class, Stifle.class})
class WiitigoTest extends BaseCardTest {

    @Test
    @DisplayName("Wiitigo enters the battlefield with six +1/+1 counters")
    void entersWithSixCounters() {
        harness.setHand(player1, List.of(new Wiitigo()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wiitigo = findPermanent(player1, "Wiitigo");
        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(wiitigo.getEffectivePower()).isEqualTo(6);
        assertThat(wiitigo.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Upkeep removes a +1/+1 counter when Wiitigo hasn't been in a block")
    void upkeepRemovesCounterWithoutBlock() {
        Permanent wiitigo = addWiitigo(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Wiitigo's upkeep ability triggers only during its controller's upkeep")
    void upkeepDoesNotTriggerOnOpponentUpkeep() {
        Permanent wiitigo = addWiitigo(player1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Upkeep adds a +1/+1 counter after Wiitigo blocked")
    void upkeepAddsCounterAfterBlocking() {
        Permanent wiitigo = addWiitigo(player2);
        addCreatureReady(player1, new BalduvianBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Upkeep adds a +1/+1 counter after Wiitigo was blocked")
    void upkeepAddsCounterAfterBeingBlocked() {
        Permanent wiitigo = addWiitigo(player1);
        addCreatureReady(player2, new BalduvianBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("The block window is consumed, so a later upkeep with no block removes a counter again")
    void blockWindowIsConsumedByTheUpkeepTrigger() {
        Permanent wiitigo = addWiitigo(player2);
        addCreatureReady(player1, new BalduvianBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Attacking without being blocked does not earn an upkeep counter")
    void unblockedAttackDoesNotEarnCounter() {
        Permanent wiitigo = addWiitigo(player1);

        declareAttackers(List.of(0));
        resolveCombat();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing the last counter sends Wiitigo to the graveyard")
    void diesWhenUpkeepRemovesLastCounter() {
        Permanent wiitigo = addWiitigo(player1);
        wiitigo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wiitigo");
        harness.assertInGraveyard(player1, "Wiitigo");
    }

    @Test
    @DisplayName("An opponent's upkeep does not consume Wiitigo's blocking history")
    void opponentUpkeepPreservesBlockingHistory() {
        Permanent wiitigo = addWiitigo(player2);
        addCreatureReady(player1, new BalduvianBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Countering the upkeep ability does not extend the blocking-history window")
    void counteredUpkeepStillEndsPreviousBlockingWindow() {
        Permanent wiitigo = addWiitigo(player2);
        addCreatureReady(player1, new BalduvianBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        var upkeepAbilityId = gd.stack.getLast().getTargetableId();
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, upkeepAbilityId);
        assertThat(gd.stack).isEmpty();
        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private Permanent addWiitigo(Player player) {
        Permanent wiitigo = addCreatureReady(player, new Wiitigo());
        wiitigo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        return wiitigo;
    }
}
