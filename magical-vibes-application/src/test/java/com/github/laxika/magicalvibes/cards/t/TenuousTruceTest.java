package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TenuousTruce.class, GrizzlyBears.class, JaceBeleren.class})
class TenuousTruceTest extends BaseCardTest {

    @Test
    @DisplayName("Tenuous Truce can be cast enchanting an opponent")
    void enchantsOpponentWhenCast() {
        harness.setHand(player1, List.of(new TenuousTruce()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tenuous Truce").getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Tenuous Truce cannot target its controller")
    void cannotEnchantController() {
        harness.setHand(player1, List.of(new TenuousTruce()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Tenuous Truce");
    }

    @Test
    @DisplayName("The draw ability does not trigger at its controller's end step")
    void doesNotTriggerAtControllersEndStep() {
        attachTo(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw ability still resolves after Tenuous Truce leaves the battlefield")
    void drawsAfterAuraLeavesBattlefield() {
        Permanent aura = attachTo(player2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 1);
    }

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
        harness.assertInGraveyard(player1, "Tenuous Truce");
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
        harness.assertInGraveyard(player1, "Tenuous Truce");
    }

    @Test
    @DisplayName("The enchanted opponent attacking you or your planeswalker sacrifices Tenuous Truce")
    void sacrificesWhenEnchantedOpponentAttacksController() {
        Permanent aura = attachTo(player2);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttack(player2, attacker, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        harness.assertInGraveyard(player1, "Tenuous Truce");
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
        harness.assertInGraveyard(player1, "Tenuous Truce");
    }

    @Test
    @DisplayName("The enchanted opponent draws before the controller at their end step")
    void activeEnchantedOpponentDrawsFirst() {
        attachTo(player2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        int logStart = gd.gameLog.size();
        String controllerDraw = gd.playerIdToName.get(player1.getId()) + " draws a card.";
        String enchantedPlayerDraw = gd.playerIdToName.get(player2.getId()) + " draws a card.";

        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.gameLog.subList(logStart, gd.gameLog.size()).stream()
                .map(entry -> entry.plainText())
                .filter(text -> text.equals(controllerDraw) || text.equals(enchantedPlayerDraw))
                .toList()).containsExactly(enchantedPlayerDraw, controllerDraw);
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
