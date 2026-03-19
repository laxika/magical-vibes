package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CathedralMembraneTest extends BaseCardTest {

    /**
     * Sets up combat where Cathedral Membrane (player2, defender) blocks a creature (player1, attacker).
     * Player1 attacks with a creature at index 0, and Cathedral Membrane blocks it.
     */
    private void setupCombatWhereMembraneBlocks(Permanent attackerPerm, Permanent membranePerm) {
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);

        membranePerm.setSummoningSick(false);
        membranePerm.setBlocking(true);
        membranePerm.addBlockingTarget(0);
        membranePerm.addBlockingTargetId(attackerPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Cathedral Membrane has ON_DEATH DealDamageToBlockedAttackersOnDeathEffect(6)")
    void hasCorrectEffect() {
        CathedralMembrane card = new CathedralMembrane();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(DealDamageToBlockedAttackersOnDeathEffect.class);
        DealDamageToBlockedAttackersOnDeathEffect effect =
                (DealDamageToBlockedAttackersOnDeathEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.damage()).isEqualTo(6);
    }

    // ===== Death trigger during combat =====

    @Test
    @DisplayName("When Cathedral Membrane dies in combat, it deals 6 damage to the creature it blocked")
    void deathTriggerDeals6DamageToBlockedCreature() {
        GrizzlyBears attacker = new GrizzlyBears();
        attacker.setPower(3);
        attacker.setToughness(3);
        harness.addToBattlefield(player1, attacker);

        harness.addToBattlefield(player2, new CathedralMembrane());

        Permanent attackerPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent membranePerm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cathedral Membrane"))
                .findFirst().orElseThrow();

        UUID attackerId = attackerPerm.getId();
        setupCombatWhereMembraneBlocks(attackerPerm, membranePerm);

        // Pass priority to deal combat damage — Membrane (0/3) dies to the 3/3 attacker
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Cathedral Membrane should be dead
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cathedral Membrane"));

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Cathedral Membrane")
                && e.getTargetPermanentIds().contains(attackerId));

        // Resolve the triggered ability — 6 damage to a 3/3 is lethal
        harness.passBothPriorities();

        // The attacker should be destroyed by the 6 damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(attackerId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cathedral Membrane deals 6 damage but does not kill a creature with toughness > 6")
    void deathTriggerDoesNotKillHighToughnessCreature() {
        GrizzlyBears bigAttacker = new GrizzlyBears();
        bigAttacker.setPower(3);
        bigAttacker.setToughness(7);
        harness.addToBattlefield(player1, bigAttacker);

        harness.addToBattlefield(player2, new CathedralMembrane());

        Permanent attackerPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent membranePerm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cathedral Membrane"))
                .findFirst().orElseThrow();

        UUID attackerId = attackerPerm.getId();
        setupCombatWhereMembraneBlocks(attackerPerm, membranePerm);

        harness.passBothPriorities(); // Combat damage — Membrane dies
        harness.passBothPriorities(); // Resolve trigger — 6 damage to 7 toughness is not lethal

        GameData gd = harness.getGameData();

        // Attacker should still be alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attackerId));
    }

    @Test
    @DisplayName("Cathedral Membrane does not trigger when it dies outside of combat (Wrath of God)")
    void noTriggerOutsideCombat() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CathedralMembrane());

        // Use Wrath of God to kill Cathedral Membrane outside of combat
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        GameData gd = harness.getGameData();

        // Cathedral Membrane should be dead
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cathedral Membrane"));

        // No Cathedral Membrane triggered ability should be on the stack
        // (Membrane died during precombat main, not during combat)
        assertThat(gd.stack).noneMatch(e ->
                e.getCard().getName().equals("Cathedral Membrane"));
    }
}
