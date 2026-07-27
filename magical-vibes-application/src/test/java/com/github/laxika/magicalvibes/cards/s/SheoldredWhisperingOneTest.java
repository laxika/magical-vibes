package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SheoldredWhisperingOneTest extends BaseCardTest {

    // ===== Upkeep trigger: return creature from graveyard =====

    @Test
    @DisplayName("Controller's upkeep returns creature from graveyard to battlefield")
    void upkeepReturnsCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve trigger → graveyard choice prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        // Choose creature
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns specific creature when multiple are in graveyard")
    void returnsSpecificCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new AngelOfMercy()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Choose Angel of Mercy (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Angel of Mercy");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Angel of Mercy");
    }

    @Test
    @DisplayName("No effect when graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());

        advanceToUpkeep(player1);

        // Trigger is on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve — should resolve without graveyard choice
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("No effect when graveyard has only non-creature cards")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new HolyDay()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Upkeep trigger does NOT fire during opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        // The opponent upkeep sacrifice trigger may fire, but no graveyard return trigger
        // Check that no graveyard return happens
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Opponent upkeep trigger: sacrifice creature =====

    @Test
    @DisplayName("Opponent with one creature sacrifices it on their upkeep")
    void opponentSacrificesSingleCreature() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose which to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        // Player 2 chooses to sacrifice Grizzly Bears
        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("No sacrifice effect when opponent has no creatures")
    void noSacrificeWhenOpponentHasNoCreatures() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no creatures to sacrifice"));
    }

    @Test
    @DisplayName("Sacrifice trigger does NOT fire during controller's own upkeep")
    void sacrificeTriggerDoesNotFireDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        advanceToUpkeep(player1);

        // Upkeep trigger fires (graveyard return), but not opponent sacrifice
        // The bears should still be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    // ===== Both triggers =====

    @Test
    @DisplayName("Returned creature's ETB ability triggers")
    void returnedCreatureTriggersETB() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve graveyard return trigger
        harness.handleGraveyardCardChosen(player1, 0); // return Angel of Mercy

        // Angel of Mercy's ETB (gain 3 life) should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel of Mercy");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }
}
