package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
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

@CardUsed({NecroticPlague.class, GrizzlyBears.class, WhiteKnight.class, Zephid.class})
class NecroticPlagueTest extends BaseCardTest {

    // ===== Upkeep sacrifice trigger =====

    @Test
    @DisplayName("Enchanted creature is sacrificed at the beginning of its controller's upkeep")
    void upkeepSacrificesEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castNecroticPlagueOn(player1, creature);

        // Advance to player2's upkeep (enchanted creature's controller)
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger

        // Creature should be gone from battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getId().equals(creature.getId()))).isFalse();

        // Creature should be in graveyard
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifice does NOT trigger during the aura controller's upkeep (only enchanted creature's)")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent myCreature = addCreatureReady(player1, new GrizzlyBears());
        castNecroticPlagueOn(player1, opponentCreature);

        // Advance to player2's upkeep — sacrifice trigger fires
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger — creature dies, death trigger fires
        harness.passBothPriorities(); // resolve death trigger — aura returns to player1's creature

        // Player2's creature should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()))).isTrue();

        // Necrotic Plague should be on the battlefield attached to player1's creature
        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necrotic Plague"))
                .findFirst().orElse(null);
        assertThat(auraPerm).isNotNull();
        assertThat(auraPerm.getAttachedTo()).isEqualTo(myCreature.getId());

        // Necrotic Plague should NOT be in any graveyard
        harness.assertNotInGraveyard(player1, "Necrotic Plague");
    }

    @Test
    @DisplayName("Death trigger offers only creatures it can target and enchant")
    void deathTriggerExcludesIllegalCreatures() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstLegalCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondLegalCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent protectedCreature = addCreatureReady(player1, new WhiteKnight());
        Permanent shroudedCreature = addCreatureReady(player1, new Zephid());
        castNecroticPlagueOn(player1, enchantedCreature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
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

        Permanent aura = findPermanentByName(player1, "Necrotic Plague");
        assertThat(aura.getAttachedTo()).isEqualTo(firstLegalCreature.getId());
        harness.assertNotInGraveyard(player1, "Necrotic Plague");
    }

    @Test
    @DisplayName("Death trigger fizzles when no opponent creatures exist")
    void deathTriggerFizzlesWithNoOpponentCreatures() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        castNecroticPlagueOn(player1, opponentCreature);
        // Player1 has no creatures

        // Advance to player2's upkeep — sacrifice trigger fires
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice — creature dies, death trigger fires
        harness.passBothPriorities(); // resolve death trigger — no target, fizzles

        // Necrotic Plague should remain in the graveyard
        harness.assertInGraveyard(player1, "Necrotic Plague");

        // No Necrotic Plague on any battlefield
        for (var bf : gd.playerBattlefields.values()) {
            assertThat(bf).noneMatch(p -> p.getCard().getName().equals("Necrotic Plague"));
        }
    }

    // ===== Full cycle: plague bounces back and forth =====

    @Test
    @DisplayName("Necrotic Plague bounces between players as creatures die")
    void plagueBouncesBetweenPlayers() {
        Permanent creature2 = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        castNecroticPlagueOn(player1, creature2);

        // ---- First cycle: player2's creature dies at player2's upkeep ----
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice — creature2 dies
        harness.passBothPriorities(); // resolve death trigger — plague attaches to creature1

        // Creature2 dead, plague on creature1
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(p -> p.getId().equals(creature2.getId()))).isTrue();
        Permanent auraOnCreature1 = findPermanentByName(player1, "Necrotic Plague");
        assertThat(auraOnCreature1.getAttachedTo()).isEqualTo(creature1.getId());

        // Add a new creature for player2 so plague has somewhere to go next
        Permanent creature2b = addCreatureReady(player2, new GrizzlyBears());

        // ---- Second cycle: player1's creature dies at player1's upkeep ----
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve sacrifice — creature1 dies
        harness.passBothPriorities(); // resolve death trigger — plague attaches to creature2b

        // Creature1 dead, plague on creature2b
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getId().equals(creature1.getId()))).isTrue();
        Permanent auraOnCreature2b = findPermanentByName(player1, "Necrotic Plague");
        assertThat(auraOnCreature2b.getAttachedTo()).isEqualTo(creature2b.getId());
    }

    // ===== Helper methods =====

    private void castNecroticPlagueOn(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new NecroticPlague()));
        harness.addMana(caster, ManaColor.BLACK, 4);
        harness.castEnchantment(caster, 0, target.getId());
        harness.passBothPriorities();

        // Verify attachment
        Permanent auraPerm = findPermanentByName(caster, "Necrotic Plague");
        assertThat(auraPerm.getAttachedTo()).isEqualTo(target.getId());
    }

    private Permanent findPermanentByName(Player player, String name) {
        return findPermanent(player, name);
    }
}
