package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForbiddingSpirit.class, GrizzlyBears.class, DovinGrandArbiter.class})
class ForbiddingSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Charges two mana for each creature attacking its controller")
    void chargesTwoManaForEachAttacker() {
        Permanent bear1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player2, new GrizzlyBears());
        castSpirit();

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(bear1),
                gd.playerBattlefields.get(player2.getId()).indexOf(bear2)));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Rejects an attack without enough mana for every attacker")
    void rejectsInsufficientAttackTax() {
        Permanent bear1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player2, new GrizzlyBears());
        castSpirit();

        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(bear1),
                gd.playerBattlefields.get(player2.getId()).indexOf(bear2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Survives cleanup and expires at its controller's next turn")
    void lastsUntilControllerNextTurn() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        castSpirit();

        gd.expireEndOfTurnFloatingEffects();
        assertThatThrownBy(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(bear))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThatCode(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(bear))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Protects planeswalkers that enter after the ability resolves")
    void taxesAttacksOnPlaneswalkers() {
        castSpirit();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new DovinGrandArbiter());
        addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0),
                Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Applies to creatures entering after the trigger resolves")
    void taxesLaterCreatures() {
        castSpirit();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Multiple resolved triggers impose cumulative attack costs")
    void multipleSpiritsStack() {
        addCreatureReady(player2, new GrizzlyBears());
        castSpirit();
        castSpirit();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        declareAttackers(player2, List.of(0));
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not tax its controller's attacks on the opponent")
    void controllerCanAttackWithoutPayment() {
        addCreatureReady(player1, new GrizzlyBears());
        castSpirit();

        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The resolved protection survives the Spirit dying")
    void protectionSurvivesSourceDeath() {
        castSpirit();
        Permanent spirit = findPermanent(player1, "Forbidding Spirit");
        spirit.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(countPermanents(player1, "Forbidding Spirit")).isZero();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    private void castSpirit() {
        harness.setHand(player1, List.of(new ForbiddingSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
