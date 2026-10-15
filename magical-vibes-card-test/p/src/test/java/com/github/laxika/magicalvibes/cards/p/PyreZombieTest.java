package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PyreZombie.class)
class PyreZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{B}{B} returns Pyre Zombie from the graveyard to hand during upkeep")
    void payingUpkeepCostReturnsToHand() {
        PyreZombie zombie = new PyreZombie();
        harness.setGraveyard(player1, List.of(zombie));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(zombie.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(zombie.getId()));
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Pyre Zombie in the graveyard")
    void decliningUpkeepPaymentLeavesItInGraveyard() {
        PyreZombie zombie = new PyreZombie();
        harness.setGraveyard(player1, List.of(zombie));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(zombie.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(zombie.getId()));
    }

    @Test
    @DisplayName("The graveyard upkeep ability triggers only during its owner's upkeep")
    void upkeepAbilityTriggersOnlyDuringOwnersUpkeep() {
        harness.setGraveyard(player1, List.of(new PyreZombie()));

        advanceToUpkeep(player2);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability does not offer payment after Pyre Zombie leaves the graveyard")
    void upkeepAbilityDoesNotOfferPaymentAfterLeavingGraveyard() {
        PyreZombie zombie = new PyreZombie();
        harness.setGraveyard(player1, List.of(zombie));

        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(zombie));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNull();
    }

    @Test
    @DisplayName("Sacrificing Pyre Zombie deals 2 damage to a target player")
    void sacrificeAbilityDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new PyreZombie());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Pyre Zombie");
        harness.assertInGraveyard(player1, "Pyre Zombie");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Pyre Zombie");
    }

    @Test
    @DisplayName("Sacrificing Pyre Zombie deals 2 damage to a target creature")
    void sacrificeAbilityDealsDamageToCreature() {
        harness.addToBattlefield(player1, new PyreZombie());
        harness.addToBattlefield(player2, new PyreZombie());
        harness.addMana(player1, ManaColor.RED, 3);

        var target = harness.getPermanentId(player2, "Pyre Zombie");
        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pyre Zombie");
        harness.assertInGraveyard(player1, "Pyre Zombie");
    }

    @Test
    @DisplayName("Each Pyre Zombie in the graveyard requires its own upkeep payment")
    void paymentReturnsOnlyOneOfMultipleCopies() {
        harness.setGraveyard(player1, List.of(new PyreZombie(), new PyreZombie()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Pyre Zombie can target itself, but sacrifice makes that target illegal")
    void targetingItselfDoesNotDealDamage() {
        harness.addToBattlefield(player1, new PyreZombie());
        var target = harness.getPermanentId(player1, "Pyre Zombie");
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pyre Zombie");
        harness.assertNotOnBattlefield(player1, "Pyre Zombie");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An old upkeep trigger cannot return Pyre Zombie after it leaves and reenters the graveyard")
    void upkeepTriggerDoesNotFollowCardIntoNewGraveyardIncarnation() {
        PyreZombie zombie = new PyreZombie();
        harness.setGraveyard(player1, List.of(zombie));
        advanceToUpkeep(player1);

        // Simulate returning the card to the battlefield in response to its upkeep trigger.
        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player1, zombie);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Pyre Zombie");

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Pyre Zombie");
        harness.assertNotInHand(player1, "Pyre Zombie");
    }
}
