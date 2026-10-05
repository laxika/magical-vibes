package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IxalansBinding.class, GrizzlyBears.class, Naturalize.class, Demystify.class, PullFromEternity.class})
class IxalansBindingTest extends BaseCardTest {

    private void castAndResolveIxalansBinding(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IxalansBinding()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // resolve enchantment spell -> ETB on stack
        harness.passBothPriorities(); // resolve ETB -> exile
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }


    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls until source leaves")
    void etbExilesTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIxalansBinding(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exile return tracking is created (O-ring style)")
    void exileReturnTrackingCreated() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIxalansBinding(bearsId);

        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }


    @Test
    @DisplayName("Exiled card returns when Ixalan's Binding is destroyed")
    void exiledCardReturnsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIxalansBinding(bearsId);

        resetForFollowUpSpell();

        // Destroy Ixalan's Binding
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID bindingId = harness.getPermanentId(player1, "Ixalan's Binding");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bindingId);

        // Grizzly Bears should return to the battlefield
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exile tracking is cleaned up after source leaves")
    void exileTrackingCleanedUpAfterSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIxalansBinding(bearsId);

        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        resetForFollowUpSpell();

        // Destroy Ixalan's Binding
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID bindingId = harness.getPermanentId(player1, "Ixalan's Binding");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bindingId);

        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }


    @Test
    @DisplayName("Opponent cannot cast spells with same name as exiled card")
    void opponentCannotCastSpellsWithSameName() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIxalansBinding(bearsId);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Controller CAN cast spells with same name as exiled card (opponents only restriction)")
    void controllerCanCastSpellsWithSameName() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIxalansBinding(bearsId);

        resetForFollowUpSpell();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Spells with different names can still be cast by opponents")
    void spellsWithDifferentNamesCanStillBeCast() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIxalansBinding(bearsId);

        // Naturalize has a different name than Grizzly Bears, so it should be castable
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID bindingId = harness.getPermanentId(player1, "Ixalan's Binding");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, bindingId);

        assertThat(gd.stack).hasSize(1);
    }


    @Test
    @DisplayName("Casting restriction lifts when Ixalan's Binding is destroyed")
    void castingRestrictionLiftsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIxalansBinding(bearsId);

        resetForFollowUpSpell();

        // Destroy Ixalan's Binding
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID bindingId = harness.getPermanentId(player1, "Ixalan's Binding");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bindingId);

        // Casting restriction should be gone since Ixalan's Binding left
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Removing Binding before its enter trigger resolves does not exile the target")
    void sourceLeavesBeforeEnterTriggerResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new IxalansBinding()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        UUID bindingId = harness.getPermanentId(player1, "Ixalan's Binding");
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bindingId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Ixalan's Binding");
    }

    @Test
    @DisplayName("Binding can exile an enchantment and restrict casting enchantments with its name")
    void exilesNoncreatureAndRestrictsItsName() {
        harness.addToBattlefield(player2, new IxalansBinding());
        UUID opponentBindingId = harness.getPermanentId(player2, "Ixalan's Binding");
        castAndResolveIxalansBinding(opponentBindingId);

        harness.assertNotOnBattlefield(player2, "Ixalan's Binding");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new IxalansBinding()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The casting restriction ends when the exiled card leaves exile")
    void restrictionEndsWhenExiledCardLeavesExile() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        castAndResolveIxalansBinding(harness.getPermanentId(player2, "Grizzly Bears"));

        resetForFollowUpSpell();
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Ixalan's Binding");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
