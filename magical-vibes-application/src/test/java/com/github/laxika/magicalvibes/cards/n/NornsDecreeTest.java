package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NornsDecree.class, Forest.class, GrizzlyBears.class})
class NornsDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives one poison counter when one or more opponent creatures deal combat damage")
    void givesOnePoisonCounterForCombatDamage() {
        harness.addToBattlefield(player2, new NornsDecree());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The attacking player draws a card when attacking a poisoned player")
    void attackingPlayerDrawsAgainstPoisonedPlayer() {
        harness.addToBattlefield(player1, new NornsDecree());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        gd.playerPoisonCounters.put(player1.getId(), 1);

        declareAttackers(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when no attacked player is poisoned")
    void doesNotDrawAgainstUnpoisonedPlayer() {
        harness.addToBattlefield(player1, new NornsDecree());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void declareAttackers(Player player, int attackerIndex, UUID attackTargetId) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, List.of(attackerIndex), Map.of(attackerIndex, attackTargetId));
    }
}
