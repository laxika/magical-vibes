package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    }

    @Test
    @DisplayName("Sacrificing Pyre Zombie deals 2 damage to a target player")
    void sacrificeAbilityDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new PyreZombie());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Pyre Zombie");
    }

    @Test
    @DisplayName("Sacrificing Pyre Zombie deals 2 damage to a target creature")
    void sacrificeAbilityDealsDamageToCreature() {
        harness.addToBattlefield(player1, new PyreZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        var target = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pyre Zombie");
    }
}
