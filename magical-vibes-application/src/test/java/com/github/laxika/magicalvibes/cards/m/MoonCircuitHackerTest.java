package com.github.laxika.magicalvibes.cards.m;

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
}
