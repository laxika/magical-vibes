package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContestedWarZoneTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Contested War Zone has damage-to-controller trigger effect")
    void hasDamageToControllerTrigger() {
        ContestedWarZone card = new ContestedWarZone();

        assertThat(card.getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).getFirst())
                .isInstanceOf(DamageSourceControllerGainsControlOfThisPermanentEffect.class);

        DamageSourceControllerGainsControlOfThisPermanentEffect effect =
                (DamageSourceControllerGainsControlOfThisPermanentEffect) card.getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).getFirst();
        assertThat(effect.combatOnly()).isTrue();
        assertThat(effect.creatureOnly()).isTrue();
    }

    @Test
    @DisplayName("Contested War Zone has two activated abilities (mana and boost)")
    void hasActivatedAbilities() {
        ContestedWarZone card = new ContestedWarZone();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("Contested War Zone boost ability uses BoostAllCreaturesEffect with attacking filter")
    void boostAbilityUsesCorrectEffect() {
        ContestedWarZone card = new ContestedWarZone();

        assertThat(card.getActivatedAbilities().get(1).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(BoostAllCreaturesEffect.class);

        BoostAllCreaturesEffect boost = (BoostAllCreaturesEffect) card.getActivatedAbilities().get(1).getEffects().getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.filter()).isNotNull();
    }

    // ===== Combat damage control change =====

    @Test
    @DisplayName("Unblocked attacker dealing combat damage to controller causes control change")
    void unblockedAttackerCausesControlChange() {
        addContestedWarZone(player2);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1, player2);

        // Contested War Zone should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Contested War Zone"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Contested War Zone"));
    }

    @Test
    @DisplayName("Multiple attackers dealing combat damage only transfer control once")
    void multipleAttackersOnlyTransferOnce() {
        addContestedWarZone(player2);
        Permanent attacker1 = addReadyCreature(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addReadyCreature(player1, new GrizzlyBears());
        attacker2.setAttacking(true);

        resolveCombat(player1, player2);

        // Contested War Zone should be on player1's battlefield (transferred once)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Contested War Zone"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Contested War Zone"));
    }

    @Test
    @DisplayName("Ability damage does not trigger control change (combat only)")
    void abilityDamageDoesNotTriggerControlChange() {
        addContestedWarZone(player2);
        harness.setLife(player2, 20);
        Permanent artillery = addReadyCreature(player1, new OrcishArtillery());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Contested War Zone should still be on player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Contested War Zone"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Contested War Zone"));
    }

    @Test
    @DisplayName("Blocked attacker that deals no damage to player does not trigger control change")
    void blockedAttackerDoesNotTriggerControlChange() {
        addContestedWarZone(player2);

        // Small attacker that will die in combat
        GrizzlyBears smallAttacker = new GrizzlyBears();
        smallAttacker.setPower(1);
        smallAttacker.setToughness(1);
        Permanent attacker = addReadyCreature(player1, smallAttacker);
        attacker.setAttacking(true);

        // Big blocker that kills the attacker
        GrizzlyBears bigBlocker = new GrizzlyBears();
        bigBlocker.setPower(5);
        bigBlocker.setToughness(5);
        Permanent blocker = addReadyCreature(player2, bigBlocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1, player2);

        // Contested War Zone should still be on player2's battlefield (attacker was blocked and killed)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Contested War Zone"));
    }

    // ===== Boost ability =====

    @Test
    @DisplayName("Boost ability gives +1/+0 to attacking creatures")
    void boostAbilityGivesPlusOnePlusZero() {
        Permanent warZone = addContestedWarZone(player1);

        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setPower(2);
        bearsCard.setToughness(2);
        Permanent attacker = addReadyCreature(player1, bearsCard);
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        // Activate the second ability (index 1): {1}, {T}: Attacking creatures get +1/+0
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Attacker should have +1 power modifier
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost ability does not boost non-attacking creatures")
    void boostAbilityDoesNotBoostNonAttackingCreatures() {
        Permanent warZone = addContestedWarZone(player1);

        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setPower(2);
        bearsCard.setToughness(2);
        Permanent nonAttacker = addReadyCreature(player1, bearsCard);
        // Not attacking

        GrizzlyBears attackerCard = new GrizzlyBears();
        attackerCard.setPower(2);
        attackerCard.setToughness(2);
        Permanent attacker = addReadyCreature(player1, attackerCard);
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Non-attacking creature should not be boosted
        assertThat(nonAttacker.getPowerModifier()).isEqualTo(0);
        // Attacking creature should be boosted
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addContestedWarZone(Player player) {
        Permanent perm = new Permanent(new ContestedWarZone());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat(Player attacker, Player defender) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
