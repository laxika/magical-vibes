package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Obliterate;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TianaShipsCaretaker.class, GrizzlyBears.class, HolyStrength.class,
        Naturalize.class, ShortSword.class, Shock.class, Obliterate.class})
class TianaShipsCaretakerTest extends BaseCardTest {

    /**
     * Places an aura onto the battlefield attached to a target permanent.
     */
    private void placeAuraOnBattlefield(HolyStrength auraCard, UUID ownerPlayerId, UUID targetPermId) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(
                ownerPlayerId.equals(player1.getId()) ? player1 : player2, auraCard);
        auraPerm.setAttachedTo(targetPermId);
    }

    @Test
    @DisplayName("Destroying an Aura with Tiana on battlefield puts triggered ability on stack")
    void destroyAuraPutsTriggeredAbilityOnStack() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        placeAuraOnBattlefield(new HolyStrength(), player1.getId(), bearsPermId);

        // Destroy the aura with Naturalize
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID auraPermId = harness.getPermanentId(player1, "Holy Strength");
        harness.castInstant(player2, 0, auraPermId);
        harness.passBothPriorities(); // Resolves Naturalize

        // Tiana's triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Tiana, Ship's Caretaker"));
    }

    @Test
    @DisplayName("Accepting may ability and advancing to end step returns Aura to hand")
    void acceptMayReturnsAuraAtEndStep() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        placeAuraOnBattlefield(new HolyStrength(), player1.getId(), bearsPermId);

        // Destroy the aura with Naturalize
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID auraPermId = harness.getPermanentId(player1, "Holy Strength");
        harness.castInstant(player2, 0, auraPermId);
        harness.passBothPriorities(); // Resolves Naturalize; triggered ability goes on stack

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertNotInHand(player1, "Holy Strength");
        beginNextEndStep();
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Aura should be back in hand
        harness.assertInHand(player1, "Holy Strength");
        // And no longer in graveyard
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Declining may ability does not return Aura at end step")
    void declineMayDoesNotReturnAura() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        placeAuraOnBattlefield(new HolyStrength(), player1.getId(), bearsPermId);

        // Destroy the aura
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID auraPermId = harness.getPermanentId(player1, "Holy Strength");
        harness.castInstant(player2, 0, auraPermId);
        harness.passBothPriorities(); // Resolves Naturalize

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        beginNextEndStep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotInHand(player1, "Holy Strength");

        // Aura stays in graveyard
        harness.assertInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Aura goes to graveyard when enchanted creature dies — triggers Tiana")
    void orphanedAuraTriggersTiana() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        placeAuraOnBattlefield(new HolyStrength(), player1.getId(), bearsPermId);

        // Holy Strength makes the Bears 3/4; two Shocks are lethal.
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities(); // First Shock: 2 damage

        // The second Shock brings marked damage to four.
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities(); // Second Shock kills bears

        // Bears should be dead, aura should be in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Strength");

        // Tiana's triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Tiana, Ship's Caretaker"));
    }

    @Test
    @DisplayName("Destroying an Equipment triggers Tiana's may ability and returns it at end step")
    void destroyEquipmentTriggersMayAndReturnsAtEndStep() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new ShortSword());

        // Destroy the Equipment with Naturalize
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID swordPermId = harness.getPermanentId(player1, "Short Sword");
        harness.castInstant(player2, 0, swordPermId);
        harness.passBothPriorities(); // Resolves Naturalize

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        beginNextEndStep();
        harness.assertInGraveyard(player1, "Short Sword");
        harness.assertNotInHand(player1, "Short Sword");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Short Sword");
    }

    @Test
    @DisplayName("Without Tiana, destroying an Aura does not put triggered ability on stack")
    void noTianaNoTrigger() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        placeAuraOnBattlefield(new HolyStrength(), player1.getId(), bearsPermId);

        // Destroy the aura
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID auraPermId = harness.getPermanentId(player1, "Holy Strength");
        harness.castInstant(player2, 0, auraPermId);
        harness.passBothPriorities();

        // No triggered abilities should be on the stack
        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Card removed from graveyard before end step is not returned to hand")
    void cardRemovedFromGraveyardBeforeEndStep() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new ShortSword());

        // Destroy the Equipment
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID swordPermId = harness.getPermanentId(player1, "Short Sword");
        harness.castInstant(player2, 0, swordPermId);
        harness.passBothPriorities(); // Resolves Naturalize

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.getPermanentRemovalService().removeCardFromGraveyardById(gd,
                gd.playerGraveyards.get(player1.getId()).getFirst().getId());
        beginNextEndStep();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }
        harness.assertNotInHand(player1, "Short Sword");
    }

    private void beginNextEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);
    }

    @Test
    @DisplayName("Tiana sees Equipment dying simultaneously with her")
    void simultaneousDeathTriggersTiana() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new ShortSword());
        harness.setHand(player1, List.of(new Obliterate()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tiana, Ship's Caretaker");
        harness.assertInGraveyard(player1, "Short Sword");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Tiana, Ship's Caretaker"));
    }

    @Test
    @DisplayName("Opponent's Equipment does not trigger Tiana")
    void opponentsEquipmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player2, new ShortSword());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Short Sword"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Short Sword");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Optional return is chosen when the delayed end-step trigger resolves")
    void returnChoiceIsNotMadeBeforeEndStep() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new ShortSword());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Short Sword"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        beginNextEndStep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Short Sword");
        harness.assertNotInHand(player1, "Short Sword");
    }

    @Test
    @DisplayName("A new graveyard stay before the initial trigger resolves is not returned")
    void graveyardReentryBeforeInitialTriggerResolvesIsNotReturned() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        ShortSword sword = new ShortSword();
        harness.addToBattlefield(player1, sword);
        UUID tianaId = harness.getPermanentId(player1, "Tiana, Ship's Caretaker");
        harness.setHand(player2, List.of(new Naturalize(), new Shock(), new Shock(), new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Short Sword"));
        harness.passBothPriorities();

        // Remove Tiana in response so the second Equipment death has no new trigger.
        harness.castInstant(player2, 0, tianaId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, tianaId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Tiana, Ship's Caretaker");

        // Set up the same card returning to the battlefield while the first trigger waits.
        harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, sword.getId());
        harness.addToBattlefield(player1, sword);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Short Sword"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        // Accept an early choice if offered, isolating identity tracking from choice timing.
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }
        beginNextEndStep();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }
        harness.assertInGraveyard(player1, "Short Sword");
        harness.assertNotInHand(player1, "Short Sword");
    }

    @Test
    @DisplayName("Controlled Equipment returns to its owner rather than Tiana's controller")
    void equipmentReturnsToOwner() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        ShortSword sword = new ShortSword();
        sword.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, sword);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Short Sword"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Short Sword");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        beginNextEndStep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Short Sword");
        harness.assertNotInHand(player1, "Short Sword");
    }
}
