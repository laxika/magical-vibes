package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazormaneMasticore.class, GoblinBrawler.class, SylvokExplorer.class, WayfarersBauble.class, LoxodonWarhammer.class})
class RazormaneMasticoreTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }


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

    @Test
    @DisplayName("Draw step chooses a target before the damage decision at resolution")
    void drawStepTriggersMayAbilityPrompt() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        var target = harness.addToBattlefieldAndReturn(player2, new GoblinBrawler());

        advanceToDraw(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting draw step damage uses the target already chosen")
    void acceptingDrawStepAbilityUsesDeclaredTarget() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        var target = harness.addToBattlefieldAndReturn(player2, new RazormaneMasticore());

        advanceToDraw(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Razormane Masticore");
    }

    @Test
    @DisplayName("Draw step ability deals 3 damage to chosen creature and destroys it if lethal")
    void drawStepAbilityDeals3DamageAndKills() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        var target = harness.addToBattlefieldAndReturn(player2, new GoblinBrawler());

        advanceToDraw(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goblin Brawler");
        harness.assertInGraveyard(player2, "Goblin Brawler");
    }

    @Test
    @DisplayName("Declining draw step ability leaves the declared target undamaged")
    void decliningDrawStepAbilityDoesNothing() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        var target = harness.addToBattlefieldAndReturn(player2, new GoblinBrawler());

        advanceToDraw(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Goblin Brawler");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Draw step trigger only fires on controller's draw step")
    void drawStepTriggerOnlyOnControllersDrawStep() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GoblinBrawler());

        advanceToDraw(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Goblin Brawler");
    }

    @Test
    @DisplayName("Draw step ability can target own creatures")
    void drawStepAbilityCanTargetOwnCreatures() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        var target = harness.addToBattlefieldAndReturn(player1, new SylvokExplorer());

        advanceToDraw(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Sylvok Explorer");
        harness.assertInGraveyard(player1, "Sylvok Explorer");
    }

    @Test
    @DisplayName("Draw step ability only offers creature targets, including itself")
    void drawStepAbilityOnlyOffersCreatureTargets() {
        var masticore = harness.addToBattlefieldAndReturn(player1, new RazormaneMasticore());
        var bauble = harness.addToBattlefieldAndReturn(player2, new WayfarersBauble());

        advanceToDraw(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).containsExactly(masticore.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(bauble.getId());
        harness.handlePermanentChosen(player1, masticore.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(masticore.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Razormane Masticore");
    }

    @Test
    @DisplayName("Each Masticore requires a separate upkeep discard")
    void twoMasticoresRequireTwoDiscards() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of(new GoblinBrawler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Razormane Masticore");
        harness.assertInGraveyard(player1, "Razormane Masticore");
        harness.assertInGraveyard(player1, "Goblin Brawler");
    }
    @Test
    @DisplayName("Draw step damage gains life when Masticore has lifelink from equipment")
    void drawStepDamageUsesMasticoresGrantedLifelink() {
        var masticore = harness.addToBattlefieldAndReturn(player1, new RazormaneMasticore());
        harness.addToBattlefield(player1, new LoxodonWarhammer());
        var target = harness.addToBattlefieldAndReturn(player2, new RazormaneMasticore());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, masticore.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 20);

        advanceToDraw(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 23);
    }

}
