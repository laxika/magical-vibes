package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent urchins = addCreatureReady(player2, new DuskUrchins());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(urchins.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card for each -1/-1 counter on it when it dies")
    void drawsPerCounterOnDeath() {
        Permanent urchins = harness.addToBattlefieldAndReturn(player1, new DuskUrchins());
        urchins.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2); // now 2/1
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, urchins.getId());
        harness.passBothPriorities(); // resolve Shock -> Dusk Urchins dies, death trigger onto stack
        harness.passBothPriorities(); // resolve death trigger -> draw 2

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dusk Urchins"));
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
