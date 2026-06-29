package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NecroticPlagueTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Necrotic Plague is an aura with upkeep sacrifice and death return triggers")
    void hasCorrectEffects() {
        NecroticPlague card = new NecroticPlague();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(SacrificeEnchantedCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD).getFirst())
                .isInstanceOf(ReturnSourceAuraToOpponentCreatureOnDeathEffect.class);
    }

    // ===== Upkeep sacrifice trigger =====

    @Test
    @DisplayName("Enchanted creature is sacrificed at the beginning of its controller's upkeep")
    void upkeepSacrificesEnchantedCreature() {
        Permanent creature = addCreatureReady(player2);
        castNecroticPlagueOn(player1, creature);

        // Advance to player2's upkeep (enchanted creature's controller)
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger

        // Creature should be gone from battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getId().equals(creature.getId()))).isFalse();

        // Creature should be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sacrifice does NOT trigger during the aura controller's upkeep (only enchanted creature's)")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2);
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
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent myCreature = addCreatureReady(player1);
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Necrotic Plague"));
    }

    @Test
    @DisplayName("Death trigger fizzles when no opponent creatures exist")
    void deathTriggerFizzlesWithNoOpponentCreatures() {
        Permanent opponentCreature = addCreatureReady(player2);
        castNecroticPlagueOn(player1, opponentCreature);
        // Player1 has no creatures

        // Advance to player2's upkeep — sacrifice trigger fires
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice — creature dies, death trigger fires
        harness.passBothPriorities(); // resolve death trigger — no target, fizzles

        // Necrotic Plague should remain in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Necrotic Plague"));

        // No Necrotic Plague on any battlefield
        for (var bf : gd.playerBattlefields.values()) {
            assertThat(bf).noneMatch(p -> p.getCard().getName().equals("Necrotic Plague"));
        }
    }

    // ===== Full cycle: plague bounces back and forth =====

    @Test
    @DisplayName("Necrotic Plague bounces between players as creatures die")
    void plagueBouncesBetweenPlayers() {
        Permanent creature2 = addCreatureReady(player2);
        Permanent creature1 = addCreatureReady(player1);
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
        Permanent creature2b = addCreatureReady(player2);

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

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

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
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
