package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskUrchins.class, SafeholdSentry.class, FlameJavelin.class})
class DuskUrchinsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a -1/-1 counter when it attacks")
    void getsCounterWhenAttacking() {
        Permanent urchins = addCreatureReady(player1, new DuskUrchins());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(urchins.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, urchins)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, urchins)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a -1/-1 counter when it blocks")
    void getsCounterWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new SafeholdSentry());
        attacker.setAttacking(true);
        Permanent urchins = addCreatureReady(player2, new DuskUrchins());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(urchins.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card for each -1/-1 counter on it when it dies")
    void drawsPerCounterOnDeath() {
        Permanent urchins = harness.addToBattlefieldAndReturn(player1, new DuskUrchins());
        urchins.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2); // now 2/1
        harness.setLibrary(player1, List.of(new SafeholdSentry(), new SafeholdSentry(), new SafeholdSentry()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, urchins.getId());
        resolveAllTriggers(); // resolve Flame Javelin, then the death trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Dusk Urchins");
    }

    @Test
    @DisplayName("Draws for counters when -1/-1 counters cause it to die")
    void drawsWhenCountersCauseStateBasedDeath() {
        Permanent urchins = harness.addToBattlefieldAndReturn(player1, new DuskUrchins());
        urchins.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        harness.setLibrary(player1, List.of(new SafeholdSentry(), new SafeholdSentry(), new SafeholdSentry()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        harness.assertInGraveyard(player1, "Dusk Urchins");
    }
}
