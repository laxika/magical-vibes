package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EvilPresence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvilPresence.class, GrizzlyBears.class, HolyStrength.class, NomadMythmaker.class, Pacifism.class})
class NomadMythmakerTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Nomad Mythmaker puts it on the stack")
    void castingPutsItOnStack() {
        NomadMythmaker card = new NomadMythmaker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Resolving Nomad Mythmaker puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        NomadMythmaker card = new NomadMythmaker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability targeting Aura in graveyard puts ability on stack")
    void activatingAbilityPutsOnStack() {
        Permanent mythmakerPerm = addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player2, holyStrength);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(mythmakerPerm.getCard());
        assertThat(entry.getTargetId()).isEqualTo(holyStrength.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Activating ability taps the permanent")
    void activatingAbilityTapsPermanent() {
        Permanent mythmakerPerm = addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);

        assertThat(mythmakerPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability consumes {W} mana")
    void activatingAbilityConsumesMana() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    // ===== Resolve ability =====

    @Test
    @DisplayName("Resolving ability removes Aura from graveyard and prompts creature choice")
    void resolvingAbilityPromptsCreatureChoice() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player2, holyStrength);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(creature.getId());
        assertThat(gd.interaction.pendingAuraCard()).isSameAs(holyStrength);

        // Aura removed from graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card == holyStrength);
    }

    @Test
    @DisplayName("Choosing a creature attaches the Aura to it")
    void choosingCreatureAttachesAura() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Choose the creature
        harness.handlePermanentChosen(player1, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.pendingAuraCard()).isNull();

        // Aura should be on the battlefield attached to the creature
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent auraPerm = battlefield.stream()
                .filter(p -> p.getCard() == holyStrength)
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears()); // GrizzlyBears 2/2
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        GameData gd = harness.getGameData();
        // Holy Strength gives +1/+2, Grizzly Bears is 2/2 Ă˘â€ â€™ should be 3/4
        // Static bonuses are computed on-the-fly by GameService, not stored on Permanent
        assertThat(harness.getGameQueryService().getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target Aura in opponent's graveyard")
    void canTargetAuraInOpponentsGraveyard() {
        addMythmakerReady(player1);
        Card pacifism = new Pacifism();
        addToGraveyard(player2, pacifism);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, pacifism.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        GameData gd = harness.getGameData();
        // Pacifism should be on player1's battlefield (under their control)
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield)
                .anyMatch(p -> p.getCard() == pacifism && p.getAttachedTo().equals(creature.getId()));
        // Removed from player2's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card == pacifism);
    }

    @Test
    @DisplayName("Multiple creatures available gives choice among all of them")
    void multipleCreaturesGivesChoice() {
        Permanent mythmakerPerm = addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // The source is also a creature; the opponent's creature must not be a legal choice.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(mythmakerPerm.getId(), creature1.getId(), creature2.getId())
                .doesNotContain(opponentCreature.getId());
    }

    // ===== Fizzle cases =====

    @Test
    @DisplayName("Ability fizzles if Aura is removed from graveyard before resolution")
    void abilityFizzlesIfAuraRemovedFromGraveyard() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);

        // Remove the Aura from graveyard before resolution
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Ability fizzles if no creatures on controller's battlefield when resolving")
    void abilityFizzlesIfNoCreaturesWhenResolving() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        // Only creature is the Mythmaker itself Ă˘â‚¬â€ť remove it before resolution
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);

        // Remove all creatures before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(holyStrength);
    }

    @Test
    @CardUsed(EvilPresence.class)
    @DisplayName("Aura remains in graveyard when it cannot enchant a creature")
    void auraWithIncompatibleEnchantRestrictionRemainsInGraveyard() {
        addMythmakerReady(player1);
        Card evilPresence = new EvilPresence();
        addToGraveyard(player1, evilPresence);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, evilPresence.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(evilPresence);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == evilPresence);
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability without a target")
    void cannotActivateWithoutTarget() {
        addMythmakerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    @Test
    @DisplayName("Cannot activate ability without graveyard target zone")
    void cannotActivateWithoutGraveyardZone() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        harness.addMana(player1, ManaColor.WHITE, 1);

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
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent mythmakerPerm = addMythmakerReady(player1);
        mythmakerPerm.tap();
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD))
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
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD))
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

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot choose invalid permanent")
    void cannotChooseInvalidPermanent() {
        addMythmakerReady(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    // ===== Helper methods =====

    private Permanent addMythmakerReady(Player player) {
        return addCreatureReady(player, new NomadMythmaker());
    }

    private void addToGraveyard(Player player, Card card) {
        harness.getGameData().playerGraveyards.get(player.getId()).add(card);
    }

    @Test
    @DisplayName("Attachment choice excludes creatures controlled by the opponent")
    void attachmentChoiceExcludesOpponentsCreatures() {
        addMythmakerReadyForJudReview(player1);
        Card holyStrength = new HolyStrength();
        addToGraveyard(player1, holyStrength);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, holyStrength.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
    }

    private Permanent addMythmakerReadyForJudReview(Player player) {
        NomadMythmaker card = new NomadMythmaker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
