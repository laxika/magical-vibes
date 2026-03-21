package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TianaShipsCaretakerTest extends BaseCardTest {

    /**
     * Places an aura onto the battlefield attached to a target permanent.
     */
    private void placeAuraOnBattlefield(HolyStrength auraCard, UUID ownerPlayerId, UUID targetPermId) {
        Permanent auraPerm = new Permanent(auraCard);
        auraPerm.setAttachedTo(targetPermId);
        gd.playerBattlefields.get(ownerPlayerId).add(auraPerm);
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Has RegisterDelayedReturnCardFromGraveyardToHandEffect on the correct slot")
    void hasCorrectStructure() {
        TianaShipsCaretaker card = new TianaShipsCaretaker();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).getFirst())
                .isInstanceOf(RegisterDelayedReturnCardFromGraveyardToHandEffect.class);
    }

    // ===== Aura destroyed directly =====

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

        // Resolve Tiana's triggered ability — prompts may choice
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Accept Tiana's trigger
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the RegisterDelayedReturnCardFromGraveyardToHandEffect on the stack
        harness.passBothPriorities();

        // The delayed return should be registered
        assertThat(gd.pendingDelayedGraveyardToHandReturns).hasSize(1);

        // Advance to end step to trigger the delayed return
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        // Aura should be back in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Holy Strength"));
        // And no longer in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Holy Strength"));
        // Pending list should be cleared
        assertThat(gd.pendingDelayedGraveyardToHandReturns).isEmpty();
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

        // Resolve Tiana's triggered ability — prompts may choice
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Decline Tiana's trigger
        harness.handleMayAbilityChosen(player1, false);

        // No delayed return should be registered
        assertThat(gd.pendingDelayedGraveyardToHandReturns).isEmpty();

        // Aura stays in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Holy Strength"));
    }

    // ===== Orphaned aura (creature dies) =====

    @Test
    @DisplayName("Aura goes to graveyard when enchanted creature dies — triggers Tiana")
    void orphanedAuraTriggersTiana() {
        harness.addToBattlefield(player1, new TianaShipsCaretaker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        placeAuraOnBattlefield(new HolyStrength(), player1.getId(), bearsPermId);

        // Kill the creature with two Shocks (2/2 + Holy Strength = 3/3)
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities(); // First Shock: 2 damage

        // Cast second Shock (bears at 3 toughness with Holy Strength, 2 damage marked, 1 more needed)
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities(); // Second Shock kills bears

        // Bears should be dead, aura should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Holy Strength"));

        // Tiana's triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Tiana, Ship's Caretaker"));
    }

    // ===== Equipment destroyed =====

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

        // Resolve Tiana's triggered ability — prompts may choice
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Accept and resolve
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.pendingDelayedGraveyardToHandReturns).hasSize(1);

        // Advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Short Sword"));
    }

    // ===== No Tiana = no trigger =====

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
        assertThat(gd.pendingDelayedGraveyardToHandReturns).isEmpty();
    }

    // ===== Card removed from graveyard before end step =====

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

        // Resolve Tiana's triggered ability — prompts may choice
        harness.passBothPriorities();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.pendingDelayedGraveyardToHandReturns).hasSize(1);

        // Manually remove the card from graveyard (simulating exile or other effect)
        gd.playerGraveyards.get(player1.getId()).clear();

        // Advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        // Card should NOT be in hand (it was removed from graveyard)
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Short Sword"));
    }
}
