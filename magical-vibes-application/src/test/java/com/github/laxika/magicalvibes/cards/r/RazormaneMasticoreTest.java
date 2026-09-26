package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GoblinBrawler;
import com.github.laxika.magicalvibes.cards.s.SylvokExplorer;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazormaneMasticore.class, GoblinBrawler.class, SylvokExplorer.class, WayfarersBauble.class})
class RazormaneMasticoreTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    // ===== Upkeep — sacrifice unless discard any card =====

    @Test
    @DisplayName("Upkeep with card in hand — prompts may ability choice")
    void upkeepWithCardInHandPromptsMayAbility() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of(new GoblinBrawler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting upkeep discard keeps Masticore and discards the card")
    void acceptingUpkeepDiscardKeepsMasticore() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of(new GoblinBrawler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0); // discard the card

        // Masticore is still on the battlefield
        harness.assertOnBattlefield(player1, "Razormane Masticore");

        // Goblin Brawler is in the graveyard
        harness.assertInGraveyard(player1, "Goblin Brawler");
    }

    @Test
    @DisplayName("Any card type can be discarded for upkeep cost (not limited to creatures)")
    void anyCardTypeCanBeDiscardedForUpkeep() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of(new WayfarersBauble(), new GoblinBrawler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.handleMayAbilityChosen(player1, true);

        // Both a noncreature artifact and a creature can be discarded.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactlyInAnyOrder(0, 1);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Razormane Masticore");
        harness.assertInGraveyard(player1, "Wayfarer's Bauble");
    }

    @Test
    @DisplayName("Declining upkeep discard sacrifices Masticore")
    void decliningUpkeepDiscardSacrificesMasticore() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of(new GoblinBrawler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.handleMayAbilityChosen(player1, false);

        // Masticore is NOT on the battlefield
        harness.assertNotOnBattlefield(player1, "Razormane Masticore");

        // Masticore is in the graveyard
        harness.assertInGraveyard(player1, "Razormane Masticore");
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has empty hand")
    void autoSacrificesWithEmptyHand() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of()); // empty hand

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — auto-sacrifice

        // Masticore is NOT on the battlefield
        harness.assertNotOnBattlefield(player1, "Razormane Masticore");

        // Masticore is in the graveyard
        harness.assertInGraveyard(player1, "Razormane Masticore");

        // No prompt — no cards to discard
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Upkeep trigger only fires on controller's upkeep, not opponent's")
    void upkeepTriggerOnlyOnControllersUpkeep() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of());

        // Advance to opponent's upkeep — Masticore should NOT trigger
        advanceToUpkeep(player2);

        // Masticore should still be on the battlefield (no trigger)
        harness.assertOnBattlefield(player1, "Razormane Masticore");
    }

    // ===== Draw step — may deal 3 damage to target creature =====

    @Test
    @DisplayName("Draw step triggers may ability prompt for controller")
    void drawStepTriggersMayAbilityPrompt() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GoblinBrawler());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting draw step ability prompts for target creature")
    void acceptingDrawStepAbilityPromptsForTarget() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GoblinBrawler());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Draw step ability deals 3 damage to chosen creature and destroys it if lethal")
    void drawStepAbilityDeals3DamageAndKills() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GoblinBrawler());
        UUID brawlerId = harness.getPermanentId(player2, "Goblin Brawler");

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, brawlerId);

        // Goblin Brawler (2/2) should be destroyed by 3 damage
        harness.assertNotOnBattlefield(player2, "Goblin Brawler");
        harness.assertInGraveyard(player2, "Goblin Brawler");
    }

    @Test
    @DisplayName("Declining draw step ability does nothing")
    void decliningDrawStepAbilityDoesNothing() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GoblinBrawler());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        harness.handleMayAbilityChosen(player1, false);

        // Goblin Brawler should still be on the battlefield
        harness.assertOnBattlefield(player2, "Goblin Brawler");
    }

    @Test
    @DisplayName("Draw step trigger only fires on controller's draw step, not opponent's")
    void drawStepTriggerOnlyOnControllersDrawStep() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GoblinBrawler());

        // Advance to opponent's draw step — Masticore should NOT trigger
        advanceToDraw(player2);

        // No may ability prompt should fire for Masticore
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Goblin Brawler should still be on the battlefield
        harness.assertOnBattlefield(player2, "Goblin Brawler");
    }

    @Test
    @DisplayName("Draw step ability can target own creatures")
    void drawStepAbilityCanTargetOwnCreatures() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        SylvokExplorer elves = new SylvokExplorer();
        harness.addToBattlefield(player1, elves);
        UUID elvesId = harness.getPermanentId(player1, "Sylvok Explorer");

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elvesId);

        // Sylvok Explorer (1/1) should be destroyed by 3 damage
        harness.assertNotOnBattlefield(player1, "Sylvok Explorer");
        harness.assertInGraveyard(player1, "Sylvok Explorer");
    }

    @Test
    @DisplayName("Draw step ability only offers creature targets")
    void drawStepAbilityOnlyOffersCreatureTargets() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        UUID masticoreId = harness.getPermanentId(player1, "Razormane Masticore");
        harness.addToBattlefield(player2, new WayfarersBauble());
        UUID baubleId = harness.getPermanentId(player2, "Wayfarer's Bauble");

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        // The may ability prompt appears even though the Masticore is its only creature target.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(masticoreId);
        assertThat(choice.validPermanentIds()).doesNotContain(baubleId);
    }
}
