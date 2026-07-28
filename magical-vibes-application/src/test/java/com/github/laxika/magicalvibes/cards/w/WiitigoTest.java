package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("Upkeep adds a +1/+1 counter after Wiitigo blocked")
    void upkeepAddsCounterAfterBlocking() {
        Permanent wiitigo = addWiitigo(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Upkeep adds a +1/+1 counter after Wiitigo was blocked")
    void upkeepAddsCounterAfterBeingBlocked() {
        Permanent wiitigo = addWiitigo(player1);
        wiitigo.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("The block window is consumed, so a later upkeep with no block removes a counter again")
    void blockWindowIsConsumedByTheUpkeepTrigger() {
        Permanent wiitigo = addWiitigo(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(wiitigo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    private Permanent addWiitigo(Player player) {
        Permanent wiitigo = addCreatureReady(player, new Wiitigo());
        wiitigo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        return wiitigo;
    }
}
