package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuraGraftTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Aura Graft has correct card properties")
    void hasCorrectProperties() {
        AuraGraft card = new AuraGraft();

        assertThat(card.getName()).isEqualTo("Aura Graft");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(GainControlOfTargetAuraEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Aura Graft targeting an aura puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), creature);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Aura Graft");
        assertThat(entry.getTargetPermanentId()).isEqualTo(aura.getId());
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Cannot target a creature with Aura Graft")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a permanent");
    }

    @Test
    @DisplayName("Cannot target a non-aura enchantment")
    void cannotTargetNonAuraEnchantment() {
        // Glorious Anthem is a non-aura enchantment — add it directly
        com.github.laxika.magicalvibes.cards.g.GloriousAnthem anthem = new com.github.laxika.magicalvibes.cards.g.GloriousAnthem();
        Permanent anthemPerm = new Permanent(anthem);
        gd.playerBattlefields.get(player2.getId()).add(anthemPerm);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, anthemPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a permanent");
    }

    @Test
    @DisplayName("Cannot target an aura that is not attached to anything")
    void cannotTargetUnattachedAura() {
        // Create an aura on the battlefield that isn't attached (orphan state)
        Permanent aura = new Permanent(new HolyStrength());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a permanent");
    }

    // ===== Resolution: gain control + reattach =====

    @Test
    @DisplayName("Resolving gains control of opponent's aura and prompts reattachment")
    void resolvingGainsControlAndPromptsReattachment() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);
        Permanent myCreature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        // Aura should now be on player1's battlefield (gained control)
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);

        // Should prompt for permanent choice to reattach
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.AuraGraft.class);
        assertThat(((PermanentChoiceContext.AuraGraft) gd.interaction.permanentChoiceContext()).auraPermanentId()).isEqualTo(aura.getId());
    }

    @Test
    @DisplayName("Choosing a creature reattaches the aura")
    void choosingCreatureReattachesAura() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);
        Permanent myCreature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        // Choose my creature to attach to
        harness.handlePermanentChosen(player1, myCreature.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(myCreature.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Can reattach aura to opponent's creature")
    void canReattachToOpponentCreature() {
        Permanent opponentCreature1 = addCreatureReady(player2);
        Permanent opponentCreature2 = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new Pacifism(), opponentCreature1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        // Reattach to the other opponent creature
        harness.handlePermanentChosen(player1, opponentCreature2.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(opponentCreature2.getId());
    }

    @Test
    @DisplayName("Valid choices exclude the creature the aura is currently attached to")
    void validChoicesExcludeCurrentTarget() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);
        Permanent myCreature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        // The currently enchanted creature should NOT be a valid choice
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).doesNotContain(opponentCreature.getId());
        // My creature should be valid
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(myCreature.getId());
    }

    @Test
    @DisplayName("Static effects transfer to new creature after reattachment")
    void staticEffectsTransferAfterReattachment() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);
        Permanent myCreature = addCreatureReady(player1); // GrizzlyBears 2/2

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, myCreature.getId());

        // Holy Strength gives +1/+2, Grizzly Bears is 2/2 → should be 3/4
        assertThat(harness.getGameQueryService().getEffectivePower(gd, myCreature)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, myCreature)).isEqualTo(4);

        // Opponent creature should no longer get the bonus
        assertThat(harness.getGameQueryService().getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    // ===== Self-targeting (own aura) =====

    @Test
    @DisplayName("Can target own aura and reattach to different creature")
    void canTargetOwnAura() {
        Permanent myCreature1 = addCreatureReady(player1);
        Permanent myCreature2 = addCreatureReady(player1);
        Permanent aura = addAuraAttachedTo(player1, new HolyStrength(), myCreature1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        // Aura should remain on player1's battlefield (gain control is no-op for own aura)
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);

        // Should prompt for reattachment
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, myCreature2.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(myCreature2.getId());
    }

    // ===== No other valid creature: aura stays =====

    @Test
    @DisplayName("When only one creature exists, aura stays attached (no reattachment prompt)")
    void auraStaysWhenNoOtherCreatures() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        // Aura moved to player1's control
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);

        // No permanent choice should be prompted (only creature is the current target)
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();

        // Aura stays attached to the same creature
        assertThat(aura.getAttachedTo()).isEqualTo(opponentCreature.getId());

        // Log should mention it stays attached
        assertThat(gd.gameLog).anyMatch(log -> log.contains("stays attached"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Aura Graft fizzles if target aura is removed before resolution")
    void fizzlesIfAuraRemoved() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());

        // Remove the aura before resolution
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Aura Graft should go to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aura Graft"));
    }

    @Test
    @DisplayName("Aura Graft goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);
        Permanent myCreature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, myCreature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aura Graft"));
    }

    // ===== Validation errors for permanent choice =====

    @Test
    @DisplayName("Cannot choose invalid permanent during reattachment")
    void cannotChooseInvalidPermanent() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);
        Permanent myCreature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Wrong player cannot choose permanent during reattachment")
    void wrongPlayerCannotChoosePermanent() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);
        Permanent myCreature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, myCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    @Test
    @DisplayName("Multiple creatures on both sides are all valid choices except current target")
    void multipleCreaturesAllValid() {
        Permanent opponentCreature1 = addCreatureReady(player2);
        Permanent opponentCreature2 = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature1);
        Permanent myCreature1 = addCreatureReady(player1);
        Permanent myCreature2 = addCreatureReady(player1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingPermanentChoiceValidIds())
                .contains(opponentCreature2.getId(), myCreature1.getId(), myCreature2.getId())
                .doesNotContain(opponentCreature1.getId());
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records control change and reattachment")
    void gameLogRecordsActions() {
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent aura = addAuraAttachedTo(player2, new HolyStrength(), opponentCreature);
        Permanent myCreature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        // Log should record gaining control
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains control of") && log.contains("Holy Strength"));

        harness.handlePermanentChosen(player1, myCreature.getId());

        // Log should record reattachment
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Holy Strength") && log.contains("is now attached to") && log.contains("Grizzly Bears"));
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAuraAttachedTo(Player owner, com.github.laxika.magicalvibes.model.Card auraCard, Permanent target) {
        Permanent auraPerm = new Permanent(auraCard);
        auraPerm.setAttachedTo(target.getId());
        gd.playerBattlefields.get(owner.getId()).add(auraPerm);
        return auraPerm;
    }
}


