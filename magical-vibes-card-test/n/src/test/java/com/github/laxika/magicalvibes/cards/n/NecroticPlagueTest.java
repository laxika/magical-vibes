package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecroticPlague.class, RuneclawBear.class, WhiteKnight.class, Zephid.class, Unsummon.class, DoomBlade.class})
class NecroticPlagueTest extends BaseCardTest {

    // ===== Upkeep sacrifice trigger =====

    @Test
    @DisplayName("Enchanted creature is sacrificed at the beginning of its controller's upkeep")
    void upkeepSacrificesEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        castNecroticPlagueOn(player1, creature);

        // Advance to player2's upkeep (enchanted creature's controller)
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger

        // Creature should be gone from battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getId().equals(creature.getId()))).isFalse();

        // Creature should be in graveyard
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Sacrifice does NOT trigger during the aura controller's upkeep (only enchanted creature's)")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        castNecroticPlagueOn(player1, creature);

        // Advance to player1's upkeep (aura controller, NOT enchanted creature's controller)
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Creature should still be alive
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getId().equals(creature.getId()))).isTrue();
    }

    // ===== Death trigger — returns to opponent creature =====

    @Test
    @DisplayName("When enchanted creature dies, Necrotic Plague returns attached to an opponent's creature")
    void deathTriggerReturnsToOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());
        Permanent myCreature = addCreatureReady(player1, new RuneclawBear());
        castNecroticPlagueOn(player1, opponentCreature);

        // Advance to player2's upkeep — sacrifice trigger fires
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger — creature dies, death trigger fires
        harness.passBothPriorities(); // resolve death trigger — aura returns to player1's creature

        // Player2's creature should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()))).isTrue();

        // Necrotic Plague should be on the battlefield attached to player1's creature
        Permanent auraPerm = findPermanent(player1, "Necrotic Plague");
        assertThat(auraPerm.getAttachedTo()).isEqualTo(myCreature.getId());

        // Necrotic Plague should NOT be in any graveyard
        harness.assertNotInGraveyard(player1, "Necrotic Plague");
    }

    @Test
    @DisplayName("Death trigger offers only creatures it can target and enchant")
    void deathTriggerExcludesIllegalCreatures() {
        Permanent enchantedCreature = addCreatureReady(player2, new RuneclawBear());
        Permanent firstLegalCreature = addCreatureReady(player1, new RuneclawBear());
        Permanent secondLegalCreature = addCreatureReady(player1, new RuneclawBear());
        Permanent protectedCreature = addCreatureReady(player1, new WhiteKnight());
        Permanent shroudedCreature = addCreatureReady(player1, new Zephid());
        castNecroticPlagueOn(player1, enchantedCreature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(firstLegalCreature.getId(), secondLegalCreature.getId())
                .doesNotContain(protectedCreature.getId(), shroudedCreature.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, protectedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player2, firstLegalCreature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Necrotic Plague");
        assertThat(aura.getAttachedTo()).isEqualTo(firstLegalCreature.getId());
        harness.assertNotInGraveyard(player1, "Necrotic Plague");
    }

    @Test
    @DisplayName("Death trigger fizzles when no opponent creatures exist")
    void deathTriggerFizzlesWithNoOpponentCreatures() {
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());
        castNecroticPlagueOn(player1, opponentCreature);
        // Player1 has no creatures

        // Advance to player2's upkeep — sacrifice trigger fires
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice — creature dies, death trigger fires
        harness.passBothPriorities(); // resolve death trigger — no target, fizzles

        // Necrotic Plague should remain in the graveyard
        harness.assertInGraveyard(player1, "Necrotic Plague");

        // No Necrotic Plague on any battlefield
        harness.assertNotOnBattlefield(player1, "Necrotic Plague");
        harness.assertNotOnBattlefield(player2, "Necrotic Plague");
    }

    // ===== Full cycle: plague bounces back and forth =====

    @Test
    @DisplayName("Necrotic Plague bounces between players as creatures die")
    void plagueBouncesBetweenPlayers() {
        Permanent creature2 = addCreatureReady(player2, new RuneclawBear());
        Permanent creature1 = addCreatureReady(player1, new RuneclawBear());
        castNecroticPlagueOn(player1, creature2);

        // ---- First cycle: player2's creature dies at player2's upkeep ----
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice — creature2 dies
        harness.passBothPriorities(); // resolve death trigger — plague attaches to creature1

        // Creature2 dead, plague on creature1
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(p -> p.getId().equals(creature2.getId()))).isTrue();
        Permanent auraOnCreature1 = findPermanent(player1, "Necrotic Plague");
        assertThat(auraOnCreature1.getAttachedTo()).isEqualTo(creature1.getId());

        // Add a new creature for player2 so plague has somewhere to go next
        Permanent creature2b = addCreatureReady(player2, new RuneclawBear());

        // ---- Second cycle: player1's creature dies at player1's upkeep ----
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve sacrifice — creature1 dies
        harness.passBothPriorities(); // resolve death trigger — plague attaches to creature2b

        // Creature1 dead, plague on creature2b
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getId().equals(creature1.getId()))).isTrue();
        Permanent auraOnCreature2b = findPermanent(player1, "Necrotic Plague");
        assertThat(auraOnCreature2b.getAttachedTo()).isEqualTo(creature2b.getId());
    }

    // ===== Helper methods =====

    @Test
    @DisplayName("The enchanted creature controls and is the source of its upkeep ability")
    void upkeepAbilityBelongsToEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        castNecroticPlagueOn(player1, creature);

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("A death ability cannot switch targets when its chosen creature leaves")
    void deathAbilityDoesNotRetargetAfterChosenCreatureLeaves() {
        Permanent enchanted = addCreatureReady(player2, new RuneclawBear());
        Permanent chosen = addCreatureReady(player1, new RuneclawBear());
        Permanent other = addCreatureReady(player1, new RuneclawBear());
        castNecroticPlagueOn(player1, enchanted);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.assertInGraveyard(player1, "Necrotic Plague");
        harness.handlePermanentChosen(player2, chosen.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, chosen.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Necrotic Plague");
        harness.assertNotOnBattlefield(player1, "Necrotic Plague");
        assertThat(findPermanent(player1, "Runeclaw Bear").getId()).isEqualTo(other.getId());
    }

    @Test
    @DisplayName("The death ability also returns the Aura when the creature is destroyed")
    void returnsAfterDestructionOutsideUpkeep() {
        Permanent enchanted = addCreatureReady(player2, new RuneclawBear());
        Permanent recipient = addCreatureReady(player1, new RuneclawBear());
        castNecroticPlagueOn(player1, enchanted);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(findPermanent(player1, "Necrotic Plague").getAttachedTo()).isEqualTo(recipient.getId());
        harness.assertNotInGraveyard(player1, "Necrotic Plague");
    }

    @Test
    @DisplayName("Returning the enchanted creature to hand does not trigger the death ability")
    void doesNotReturnWhenEnchantedCreatureIsBounced() {
        Permanent enchanted = addCreatureReady(player2, new RuneclawBear());
        addCreatureReady(player1, new RuneclawBear());
        castNecroticPlagueOn(player1, enchanted);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Necrotic Plague");
        harness.assertNotOnBattlefield(player1, "Necrotic Plague");
        assertThat(gd.stack).isEmpty();
    }

    private void castNecroticPlagueOn(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new NecroticPlague()));
        harness.addMana(caster, ManaColor.BLACK, 4);
        harness.castEnchantment(caster, 0, target.getId());
        harness.passBothPriorities();

        // Verify attachment
        Permanent auraPerm = findPermanent(caster, "Necrotic Plague");
        assertThat(auraPerm.getAttachedTo()).isEqualTo(target.getId());
    }

}
