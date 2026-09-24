package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreenaTheDemagogue.class, Forest.class, GrizzlyBears.class})
class BreenaTheDemagogueTest extends BaseCardTest {

    @Test
    @DisplayName("The attacking player draws and a creature you control gets two counters")
    void rewardsAttackAgainstTheHigherLifeOpponent() {
        UUID thirdOpponentId = addThirdOpponent();
        Permanent breena = harness.addToBattlefieldAndReturn(player1, new BreenaTheDemagogue());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player2, 25);
        gd.playerLifeTotals.put(thirdOpponentId, 10);

        declareAttackers(player1, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(breena.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when the attacked opponent is not above another opponent's life")
    void doesNotRewardAttackAgainstTiedLifeTotal() {
        UUID thirdOpponentId = addThirdOpponent();
        harness.addToBattlefield(player1, new BreenaTheDemagogue());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        gd.playerLifeTotals.put(thirdOpponentId, 20);

        declareAttackers(player1, 1, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a player attacks Breena's controller")
    void doesNotRewardAttackAgainstController() {
        addThirdOpponent();
        harness.addToBattlefield(player1, new BreenaTheDemagogue());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLife(player2, 10);
        harness.setLife(player1, 25);
        gd.playerLifeTotals.put(gd.orderedPlayerIds.get(2), 5);

        declareAttackers(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private UUID addThirdOpponent() {
        UUID thirdOpponentId = UUID.randomUUID();
        gd.playerIds.add(thirdOpponentId);
        gd.orderedPlayerIds.add(thirdOpponentId);
        gd.playerBattlefields.put(thirdOpponentId, new ArrayList<>());
        gd.playerDecks.put(thirdOpponentId, new ArrayList<>());
        gd.playerHands.put(thirdOpponentId, new ArrayList<>());
        gd.playerLifeTotals.put(thirdOpponentId, 20);
        return thirdOpponentId;
    }

    private void declareAttackers(Player player, int attackerIndex, UUID attackTargetId) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, List.of(attackerIndex), Map.of(attackerIndex, attackTargetId));
    }
}
