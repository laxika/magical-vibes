package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.t.Terrarion;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecromanticThirst.class, Terrarion.class, Watchwolf.class})
class NecromanticThirstTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage requires a creature card target before resolution")
    void combatDamageRequiresGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new Watchwolf()));
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).minCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a creature card returns it from the graveyard")
    void choosingCreatureReturnsIt() {
        Watchwolf deadCreature = new Watchwolf();
        harness.setGraveyard(player1, List.of(deadCreature));
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(deadCreature.getId()));

        resolveAllTriggers();
        acceptReturn();

        harness.assertInHand(player1, "Watchwolf");
        harness.assertNotInGraveyard(player1, "Watchwolf");
    }

    @Test
    @DisplayName("The controller may decline the return when the targeted ability resolves")
    void decliningReturnLeavesTargetInGraveyard() {
        Watchwolf deadCreature = new Watchwolf();
        harness.setGraveyard(player1, List.of(deadCreature));
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(deadCreature.getId()));

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Watchwolf");
        harness.assertInGraveyard(player1, "Watchwolf");
    }

    @Test
    @DisplayName("Only creature cards are legal graveyard targets")
    void onlyCreatureCardsAreLegalTargets() {
        Terrarion artifact = new Terrarion();
        Watchwolf creatureCard = new Watchwolf();
        harness.setGraveyard(player1, List.of(artifact, creatureCard));
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creatureCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        resolveAllTriggers();
        acceptReturn();

        harness.assertInHand(player1, "Watchwolf");
        harness.assertInGraveyard(player1, "Terrarion");
    }

    @Test
    @DisplayName("A blocked enchanted creature does not trigger")
    void blockedCreatureDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new Watchwolf()));
        Permanent attacker = addCreatureReady(player1, new Watchwolf());
        attachNecromanticThirst(player1, attacker);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Watchwolf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Watchwolf");
    }

    @Test
    @DisplayName("The trigger searches only the Aura controller's graveyard")
    void triggerUsesAurasControllersGraveyard() {
        Watchwolf opponentCard = new Watchwolf();
        harness.setGraveyard(player2, List.of(opponentCard));
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Watchwolf");
    }

    @Test
    @DisplayName("An Aura controlled by one player triggers from an opponent's enchanted creature")
    void auraControllerMayReturnCardWhenOpponentsCreatureDealsDamage() {
        Watchwolf deadCreature = new Watchwolf();
        harness.setGraveyard(player1, List.of(deadCreature));
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        creature.setAttacking(true);
        creature.setAttackTarget(player1.getId());
        attachNecromanticThirst(player1, creature);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(deadCreature.getId()));
        resolveAllTriggers();
        acceptReturn();

        harness.assertInHand(player1, "Watchwolf");
        harness.assertNotInGraveyard(player1, "Watchwolf");
    }

    @Test
    @DisplayName("Casting the Aura attaches it to the targeted creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        harness.setHand(player1, List.of(new NecromanticThirst()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Necromantic Thirst").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("A target that leaves the graveyard cannot be returned")
    void targetLeavingGraveyardCannotBeReturned() {
        Watchwolf deadCreature = new Watchwolf();
        harness.setGraveyard(player1, List.of(deadCreature));
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(deadCreature.getId()));
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Watchwolf");
    }

    private void acceptReturn() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
    }

    private void attachNecromanticThirst(Player player, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new NecromanticThirst());
        aura.setAttachedTo(creature.getId());
    }
}
