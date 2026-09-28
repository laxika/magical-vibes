package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenuousTruce.class, GrizzlyBears.class, JaceBeleren.class})
class TenuousTruceTest extends BaseCardTest {

    @Test
    @DisplayName("You and the enchanted opponent draw at that opponent's end step")
    void drawsAtEnchantedOpponentsEndStep() {
        attachTo(player2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 1);
    }

    @Test
    @DisplayName("Attacking the enchanted opponent sacrifices Tenuous Truce")
    void sacrificesWhenControllerAttacksEnchantedOpponent() {
        Permanent aura = attachTo(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttack(player1, attacker, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tenuous Truce"));
    }

    @Test
    @DisplayName("Attacking a planeswalker controlled by the enchanted opponent sacrifices Tenuous Truce")
    void sacrificesWhenControllerAttacksEnchantedOpponentsPlaneswalker() {
        Permanent aura = attachTo(player2);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttack(player1, attacker, planeswalker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tenuous Truce"));
    }

    @Test
    @DisplayName("The enchanted opponent attacking you or your planeswalker sacrifices Tenuous Truce")
    void sacrificesWhenEnchantedOpponentAttacksController() {
        Permanent aura = attachTo(player2);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttack(player2, attacker, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tenuous Truce"));
    }

    @Test
    @DisplayName("The enchanted opponent attacking your planeswalker sacrifices Tenuous Truce")
    void sacrificesWhenEnchantedOpponentAttacksControllersPlaneswalker() {
        Permanent aura = attachTo(player2);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttack(player2, attacker, planeswalker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tenuous Truce"));
    }

    private Permanent attachTo(Player enchantedPlayer) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TenuousTruce());
        aura.setAttachedTo(enchantedPlayer.getId());
        return aura;
    }

    private void declareAttack(Player attackingPlayer, Permanent attacker, java.util.UUID attackTargetId) {
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(attackingPlayer.getId()).indexOf(attacker);
        gs.declareAttackers(gd, attackingPlayer, List.of(attackerIndex), Map.of(attackerIndex, attackTargetId));
    }
}
