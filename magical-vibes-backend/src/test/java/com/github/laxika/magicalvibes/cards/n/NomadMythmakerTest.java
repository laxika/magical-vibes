package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NomadMythmakerTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Nomad Mythmaker has correct card properties")
    void hasCorrectProperties() {
        NomadMythmaker card = new NomadMythmaker();

        assertThat(card.getName()).isEqualTo("Nomad Mythmaker");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.NOMAD, CardSubtype.CLERIC);
        assertThat(card.getTapActivatedAbilityEffects()).hasSize(1);
        assertThat(card.getTapActivatedAbilityEffects().getFirst())
                .isInstanceOf(ReturnAuraFromGraveyardToBattlefieldEffect.class);
        assertThat(card.getTapActivatedAbilityCost()).isEqualTo("{W}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Nomad Mythmaker puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new NomadMythmaker()));
        harness.addMana(player1, "W", 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nomad Mythmaker");
    }

    @Test
    @DisplayName("Resolving Nomad Mythmaker puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new NomadMythmaker()));
        harness.addMana(player1, "W", 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nomad Mythmaker"));
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability targeting Aura in graveyard puts ability on stack")
    void activatingAbilityPutsOnStack() {
        Permanent mythmakerPerm = addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player2, holyStrength);
        addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Nomad Mythmaker");
        assertThat(entry.getTargetPermanentId()).isEqualTo(holyStrength.getId());
        assertThat(entry.getTargetZone()).isEqualTo(TargetZone.GRAVEYARD);
    }

    @Test
    @DisplayName("Activating ability taps the permanent")
    void activatingAbilityTapsPermanent() {
        Permanent mythmakerPerm = addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);

        assertThat(mythmakerPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability consumes {W} mana")
    void activatingAbilityConsumesMana() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        addCreatureReady(player1);
        harness.addMana(player1, "W", 2);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get("W")).isEqualTo(1);
    }

    // ===== Resolve ability =====

    @Test
    @DisplayName("Resolving ability removes Aura from graveyard and prompts creature choice")
    void resolvingAbilityPromptsCreatureChoice() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player2, holyStrength);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.awaitingPermanentChoice).isTrue();
        assertThat(gd.awaitingPermanentChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.awaitingPermanentChoiceValidIds).contains(creature.getId());
        assertThat(gd.pendingAuraCard).isNotNull();
        assertThat(gd.pendingAuraCard.getName()).isEqualTo("Holy Strength");

        // Aura removed from graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Holy Strength"));
    }

    @Test
    @DisplayName("Choosing a creature attaches the Aura to it")
    void choosingCreatureAttachesAura() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);
        harness.passBothPriorities();

        // Choose the creature
        harness.handlePermanentChosen(player1, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingPermanentChoice).isFalse();
        assertThat(gd.pendingAuraCard).isNull();

        // Aura should be on the battlefield attached to the creature
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent auraPerm = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Holy Strength"))
                .findFirst()
                .orElse(null);
        assertThat(auraPerm).isNotNull();
        assertThat(auraPerm.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Aura static effect applies to attached creature after resolution")
    void auraEffectAppliesAfterResolution() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        Permanent creature = addCreatureReady(player1); // GrizzlyBears 2/2
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        GameData gd = harness.getGameData();
        // Holy Strength gives +1/+2, Grizzly Bears is 2/2 → should be 3/4
        // Static bonuses are computed on-the-fly by GameService, not stored on Permanent
        assertThat(harness.getGameService().getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(harness.getGameService().getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target Aura in opponent's graveyard")
    void canTargetAuraInOpponentsGraveyard() {
        addMythmakerReady(player1);
        Card pacifism = new Pacifism();
        addToGraveyard(player2, pacifism);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, pacifism.getId(), TargetZone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        GameData gd = harness.getGameData();
        // Pacifism should be on player1's battlefield (under their control)
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield)
                .anyMatch(p -> p.getCard().getName().equals("Pacifism") && p.getAttachedTo().equals(creature.getId()));
        // Removed from player2's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("Multiple creatures available gives choice among all of them")
    void multipleCreaturesGivesChoice() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        Permanent creature1 = addCreatureReady(player1);
        Permanent creature2 = addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both creatures should be valid choices (Mythmaker is also a creature, so 3 total)
        assertThat(gd.awaitingPermanentChoiceValidIds).contains(creature1.getId(), creature2.getId());
    }

    // ===== Fizzle cases =====

    @Test
    @DisplayName("Ability fizzles if Aura is removed from graveyard before resolution")
    void abilityFizzlesIfAuraRemovedFromGraveyard() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);

        // Remove the Aura from graveyard before resolution
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.awaitingPermanentChoice).isFalse();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Ability fizzles if no creatures on controller's battlefield when resolving")
    void abilityFizzlesIfNoCreaturesWhenResolving() {
        Permanent mythmakerPerm = addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        // Only creature is the Mythmaker itself — remove it before resolution
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);

        // Remove all creatures before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.awaitingPermanentChoice).isFalse();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability without a target")
    void cannotActivateWithoutTarget() {
        addMythmakerReady(player1);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null, TargetZone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    @Test
    @DisplayName("Cannot activate ability without graveyard target zone")
    void cannotActivateWithoutGraveyardZone() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, holyStrength.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard target");
    }

    @Test
    @DisplayName("Cannot activate ability targeting non-Aura card in graveyard")
    void cannotTargetNonAuraInGraveyard() {
        addMythmakerReady(player1);
        Card bears = new GrizzlyBears();
        addToGraveyard(player1, bears);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId(), TargetZone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent mythmakerPerm = addMythmakerReady(player1);
        mythmakerPerm.tap();
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        NomadMythmaker card = new NomadMythmaker();
        Permanent perm = new Permanent(card);
        // summoningSick is true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot choose invalid permanent")
    void cannotChooseInvalidPermanent() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);
        harness.passBothPriorities();

        // Try to choose a non-existent permanent
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, java.util.UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Wrong player cannot choose permanent")
    void wrongPlayerCannotChoosePermanent() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), TargetZone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    // ===== Helper methods =====

    private Permanent addMythmakerReady(Player player) {
        NomadMythmaker card = new NomadMythmaker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addToGraveyard(Player player, Card card) {
        harness.getGameData().playerGraveyards.get(player.getId()).add(card);
    }
}
