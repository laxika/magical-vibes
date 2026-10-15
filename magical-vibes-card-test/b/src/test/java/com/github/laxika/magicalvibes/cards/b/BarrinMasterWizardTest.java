package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrinMasterWizard.class, CoralMerfolk.class, Island.class})
class BarrinMasterWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Barrin returns the target creature to its owner's hand")
    void sacrificingSourceReturnsTargetCreature() {
        harness.addToBattlefield(player1, new BarrinMasterWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Barrin, Master Wizard");
        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        harness.assertInHand(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Can sacrifice another permanent as the activation cost")
    void sacrificingAnotherPermanentReturnsTargetCreature() {
        harness.addToBattlefield(player1, new BarrinMasterWizard());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID islandId = harness.getPermanentId(player1, "Island");
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Barrin, Master Wizard");
        harness.assertInHand(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner's hand")
    void returnsTargetToItsOwnersHand() {
        harness.addToBattlefield(player1, new BarrinMasterWizard());
        CoralMerfolk targetCard = new CoralMerfolk();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        harness.assertInHand(player1, "Coral Merfolk");
        harness.assertNotInHand(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new BarrinMasterWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can return Barrin itself by sacrificing a land while summoning sick and tapped")
    void returnsSourceBySacrificingLand() {
        Permanent barrin = harness.addToBattlefieldAndReturn(player1, new BarrinMasterWizard());
        barrin.setSummoningSick(true);
        barrin.tap();
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, barrin.getId());
        harness.handlePermanentChosen(player1, island.getId());

        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Barrin, Master Wizard");
        harness.assertNotInHand(player1, "Barrin, Master Wizard");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barrin, Master Wizard");
        harness.assertInHand(player1, "Barrin, Master Wizard");
    }

    @Test
    @DisplayName("Sacrificing the targeted creature leaves it in the graveyard")
    void sacrificingTargetMakesTargetIllegal() {
        harness.addToBattlefield(player1, new BarrinMasterWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInGraveyard(player1, "Coral Merfolk");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Barrin, Master Wizard");
        harness.assertInGraveyard(player1, "Coral Merfolk");
        harness.assertNotInHand(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("Cannot activate without paying two mana")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new BarrinMasterWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Barrin, Master Wizard");
        harness.assertNotInGraveyard(player1, "Barrin, Master Wizard");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        harness.assertNotInHand(player2, "Coral Merfolk");
    }
}
