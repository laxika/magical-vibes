package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PlagueDrone;
import com.github.laxika.magicalvibes.cards.v.VanguardSuppressor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelakorTheDarkMaster.class, PlagueDrone.class, VanguardSuppressor.class})
class BelakorTheDarkMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws and loses life for each Demon controlled")
    void entersDrawsAndLosesLifeForEachDemon() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLibrary(player1, List.of(new VanguardSuppressor(), new VanguardSuppressor()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new BelakorTheDarkMaster());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Another Demon entering deals its power to any target")
    void anotherDemonDealsItsPowerToAnyTarget() {
        harness.addToBattlefield(player1, new BelakorTheDarkMaster());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new PlagueDrone()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A non-Demon entering does not trigger the damage ability")
    void nonDemonDoesNotTriggerDamageAbility() {
        harness.addToBattlefield(player1, new BelakorTheDarkMaster());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new VanguardSuppressor());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void countsOnlyControlledDemonsAndDoesNotDamageOnItsOwnEntry() {
        harness.addToBattlefield(player2, new PlagueDrone());
        harness.setLibrary(player1, List.of(new VanguardSuppressor(), new VanguardSuppressor()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new BelakorTheDarkMaster());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsDemonsAtResolutionAfterBelakorLeaves() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLibrary(player1, List.of(new VanguardSuppressor(), new VanguardSuppressor()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        var belakor = harness.enterBattlefieldAndReturn(player1, new BelakorTheDarkMaster());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, belakor));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    void drawsAndLosesNoLifeWhenNoDemonsRemain() {
        harness.setLibrary(player1, List.of(new VanguardSuppressor()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        var belakor = harness.enterBattlefieldAndReturn(player1, new BelakorTheDarkMaster());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, belakor));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void opposingDemonDoesNotTriggerDamage() {
        harness.addToBattlefield(player1, new BelakorTheDarkMaster());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player2, new PlagueDrone());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void enteringDemonCanDamageACreature() {
        harness.addToBattlefield(player1, new BelakorTheDarkMaster());
        var target = harness.addToBattlefieldAndReturn(player2, new VanguardSuppressor());

        harness.setHand(player1, List.of(new PlagueDrone()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Vanguard Suppressor");
        harness.assertInGraveyard(player2, "Vanguard Suppressor");
    }

    @Test
    void damageUsesLastKnownPowerAfterEnteringDemonLeaves() {
        harness.addToBattlefield(player1, new BelakorTheDarkMaster());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new PlagueDrone()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var demon = findPermanent(player1, "Plague Drone");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, demon));
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }
}
