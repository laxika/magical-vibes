package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InterventionPact.class, BlindPhantasm.class, Ghostfire.class})
class InterventionPactTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the chosen source's next damage and gains that much life")
    void preventsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent source = castInterventionPact();

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 22);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Schedules the next-upkeep payment")
    void schedulesNextUpkeepPayment() {
        castInterventionPact();

        List<PayManaOrLoseGameAtNextUpkeep> scheduled =
                gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class);
        assertThat(scheduled).singleElement()
                .satisfies(action -> {
                    assertThat(action.playerId()).isEqualTo(player1.getId());
                    assertThat(action.manaCost()).isEqualTo("{1}{W}{W}");
                });
    }

    @Test
    @DisplayName("Paying at the next upkeep avoids losing the game")
    void payingAtNextUpkeepAvoidsLoss() {
        castInterventionPact();
        reachNextUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is not prevented")
    void doesNotPreventDamageFromDifferentSource() {
        harness.setLife(player1, 20);
        Permanent chosenSource = addCreatureReady(player2, new BlindPhantasm());
        Permanent otherSource = addCreatureReady(player2, new BlindPhantasm());
        castInterventionPactChoosing(chosenSource);

        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevents damage from a chosen spell on the stack and gains that much life")
    void preventsDamageFromChosenSpell() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Ghostfire ghostfire = new Ghostfire();
        harness.setHand(player2, List.of(ghostfire));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castFromHand(player1, new InterventionPact(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ghostfire.getId());
        harness.handlePermanentChosen(player1, ghostfire.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Declining at the next upkeep loses the game")
    void decliningAtNextUpkeepCausesLoss() {
        castInterventionPact();
        reachNextUpkeepPrompt();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The opponent's upkeep does not trigger the payment")
    void waitsForControllersUpkeep() {
        castInterventionPact();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).hasSize(1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The payment is owed even when no damage was prevented")
    void owesPaymentWithoutPreventingDamage() {
        harness.setLife(player1, 20);
        castInterventionPact();
        reachNextUpkeepPrompt();

        harness.assertLife(player1, 20);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The upkeep payment accepts two white mana and one colorless mana")
    void paysExactMixedManaCost() {
        castInterventionPact();
        reachNextUpkeepPrompt();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Three mana cannot pay the upkeep cost without two white mana")
    void insufficientWhiteManaCausesLoss() {
        castInterventionPact();
        reachNextUpkeepPrompt();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("An unused prevention shield expires at the end of the turn")
    void unusedShieldExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent source = castInterventionPact();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The chosen source can still deal damage to the other player")
    void doesNotPreventDamageToOtherPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent source = addCreatureReady(player1, new BlindPhantasm());
        castInterventionPactChoosing(source);

        source.setAttacking(true);
        resolveCombat(player1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private Permanent castInterventionPact() {
        Permanent source = addCreatureReady(player2, new BlindPhantasm());
        castInterventionPactChoosing(source);
        return source;
    }

    private void castInterventionPactChoosing(Permanent source) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new InterventionPact(), "{0}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
    }

    private void reachNextUpkeepPrompt() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }

}
