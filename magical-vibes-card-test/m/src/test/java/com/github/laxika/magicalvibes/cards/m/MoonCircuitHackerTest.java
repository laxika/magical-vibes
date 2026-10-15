package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonCircuitHacker.class, Forest.class, GrizzlyBears.class})
class MoonCircuitHackerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage offers a draw and then discards when the Hacker did not enter this turn")
    void combatDamageDrawsAndDiscardsWhenHackerDidNotEnterThisTurn() {
        Permanent hacker = addCreatureReady(player1, new MoonCircuitHacker());
        hacker.setAttacking(true);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Combat damage does not require a discard when the Hacker entered this turn")
    void combatDamageDoesNotDiscardWhenHackerEnteredThisTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MoonCircuitHacker()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the combat-damage trigger does not draw or discard")
    void decliningCombatDamageTriggerDoesNothing() {
        Permanent hacker = addCreatureReady(player1, new MoonCircuitHacker());
        hacker.setAttacking(true);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An older Hacker with an empty hand must discard the card it draws")
    void discardsTheDrawnCardWhenHandWasEmpty() {
        Permanent hacker = addCreatureReady(player1, new MoonCircuitHacker());
        hacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dealing combat damage only to a blocker does not offer a draw")
    void combatDamageToCreatureDoesNotTrigger() {
        addCreatureReady(player1, new MoonCircuitHacker());
        addCreatureReady(player2, new MoonCircuitHacker());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Moon-Circuit Hacker");
        harness.assertInGraveyard(player2, "Moon-Circuit Hacker");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ninjutsu returns the attacker as a cost and enters tapped attacking the same player")
    void ninjutsuReturnsAttackerAndEntersTappedAndAttacking() {
        Permanent attacker = addCreatureReady(player1, new MoonCircuitHacker());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        MoonCircuitHacker ninja = new MoonCircuitHacker();
        harness.setHand(player1, List.of(ninja));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(ninja, attacker.getCard());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        Permanent entered = findPermanent(player1, "Moon-Circuit Hacker");
        assertThat(entered.getCard()).isSameAs(ninja);
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(attacker.getCard());
    }

    @Test
    @DisplayName("Reentering before an older Hacker's trigger resolves does not remove its discard")
    void reenteringDoesNotChangeTheOriginalSourcesEntryTurn() {
        Permanent original = addCreatureReady(player1, new MoonCircuitHacker());
        original.setAttacking(true);
        original.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new MoonCircuitHacker()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.activateHandAbility(player1, 0, original.getId());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, harness::passBothPriorities);
        Permanent replacement = findPermanent(player1, "Moon-Circuit Hacker");

        harness.activateHandAbility(player1, 0, replacement.getId());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, harness::passBothPriorities);
        Permanent reentered = findPermanent(player1, "Moon-Circuit Hacker");
        assertThat(reentered.getId()).isNotEqualTo(original.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
