package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DissipationFieldTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Dissipation Field has damage-to-controller trigger effect")
    void hasDamageToControllerTrigger() {
        DissipationField card = new DissipationField();

        assertThat(card.getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).getFirst())
                .isInstanceOf(ReturnDamageSourcePermanentToHandEffect.class);
    }

    // ===== Combat damage bounce =====

    @Test
    @DisplayName("Unblocked attacker dealing combat damage to controller is bounced to owner's hand")
    void unblockedAttackerIsBounced() {
        addDissipationField(player2);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        int defenderHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player1, player2);

        // Attacker should be bounced off the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Attacker should be returned to owner's hand
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(defenderHandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Multiple unblocked attackers each trigger a separate bounce")
    void multipleAttackersEachBounced() {
        addDissipationField(player2);
        Permanent attacker1 = addReadyCreature(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addReadyCreature(player1, new GrizzlyBears());
        attacker2.setAttacking(true);

        resolveCombat(player1, player2);

        // Both attackers should be bounced
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Dissipation Field does not bounce creatures that dealt no damage (blocked and killed)")
    void blockedAttackerNotBounced() {
        addDissipationField(player2);

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

        // Attacker should be dead (in graveyard), not bounced to hand
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Spell/ability damage bounce =====

    @Test
    @DisplayName("Permanent dealing ability damage to controller triggers bounce")
    void abilityDamageTriggerssBounce() {
        addDissipationField(player2);
        harness.setLife(player2, 20);
        Permanent artillery = addReadyCreature(player1, new OrcishArtillery());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Orcish Artillery should be bounced to owner's hand
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Orcish Artillery"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Orcish Artillery"));
    }

    // ===== No trigger without Dissipation Field =====

    @Test
    @DisplayName("Without Dissipation Field, attacker is not bounced")
    void noBounceWithoutDissipationField() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1, player2);

        // Attacker should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Dissipation Field itself is not bounced by combat damage =====

    @Test
    @DisplayName("Dissipation Field itself is not bounced (it's an enchantment, not the damage source)")
    void dissipationFieldNotBounced() {
        addDissipationField(player2);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1, player2);

        // Dissipation Field should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dissipation Field"));
    }

    // ===== Helpers =====

    private void addDissipationField(Player player) {
        Permanent perm = new Permanent(new DissipationField());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
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
