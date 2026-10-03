package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskUrchins.class, SafeholdSentry.class, FlameJavelin.class, HighGround.class})
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

    @Test
    @DisplayName("Dying without -1/-1 counters draws no cards")
    void drawsNothingWithoutMinusCounters() {
        Permanent urchins = harness.addToBattlefieldAndReturn(player1, new DuskUrchins());
        urchins.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new SafeholdSentry()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, urchins.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dusk Urchins");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The attack counter can kill Dusk Urchins before combat damage and draw three cards")
    void attackCounterCausesDeathAndDrawsThree() {
        Permanent urchins = addCreatureReady(player1, new DuskUrchins());
        urchins.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setLibrary(player1, List.of(new SafeholdSentry(), new SafeholdSentry(), new SafeholdSentry()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(0)));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dusk Urchins");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    @CardUsed(HighGround.class)
    @DisplayName("Blocking multiple creatures puts only one -1/-1 counter on Dusk Urchins")
    void blockingMultipleCreaturesTriggersOnlyOnce() {
        addCreatureReady(player1, new SafeholdSentry()).setAttacking(true);
        addCreatureReady(player1, new SafeholdSentry()).setAttacking(true);
        Permanent urchins = addCreatureReady(player2, new DuskUrchins());
        harness.addToBattlefield(player2, new HighGround());

        prepareDeclareBlockers(player1);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1))));
        resolveAllTriggers();

        assertThat(urchins.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
