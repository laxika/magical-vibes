package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineSyphoner.class})
class SanguineSyphonerTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, each opponent loses 1 life and its controller gains 1 life")
    void attackDrainsEachOpponent() {
        addCreatureReady(player1, new SanguineSyphoner());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("The attack trigger drains life before combat damage even when the attacker is blocked")
    void blockedAttackStillDrainsLife() {
        addCreatureReady(player1, new SanguineSyphoner());
        addCreatureReady(player2, new SanguineSyphoner());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            resolveAllTriggers();
            harness.assertLife(player1, 21);
            harness.assertLife(player2, 19);
        });

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Only attacking copies trigger, with one life gained per attacker")
    void eachAttackingCopyDrainsOnce() {
        addCreatureReady(player1, new SanguineSyphoner());
        addCreatureReady(player1, new SanguineSyphoner());
        addCreatureReady(player1, new SanguineSyphoner());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
            harness.assertLife(player1, 22);
            harness.assertLife(player2, 18);
        });
    }

    @Test
    @DisplayName("The other player's attack gains life for that player and drains their opponent")
    void otherControllerReceivesLifeGain() {
        addCreatureReady(player2, new SanguineSyphoner());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
            harness.assertLife(player1, 19);
            harness.assertLife(player2, 21);
        });
    }
}
