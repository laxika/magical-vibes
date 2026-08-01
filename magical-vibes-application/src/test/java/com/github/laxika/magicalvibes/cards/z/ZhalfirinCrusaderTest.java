package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZhalfirinCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingHitsNonFlankingBlocker() {
        Permanent crusader = addReady(player1, new ZhalfirinCrusader());
        crusader.setAttacking(true);
        Permanent blocker = addReadyStats(player2, 2, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability registers an amount-limited redirect shield protecting itself")
    void activationCreatesShield() {
        Permanent crusader = addReady(player1, new ZhalfirinCrusader());
        Permanent destination = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, crusader), null, destination.getId());
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        var shield = gd.creatureDamageRedirectShields.getFirst();
        assertThat(shield.protectedPermanentId()).isEqualTo(crusader.getId());
        assertThat(shield.damageSourceId()).isNull();
        assertThat(shield.remainingAmount()).isEqualTo(1);
        assertThat(shield.redirectTargetId()).isEqualTo(destination.getId());
    }

    @Test
    @DisplayName("Noncombat damage to Zhalfirin Crusader is redirected to the target creature")
    void redirectsNoncombatDamageToCreature() {
        Permanent crusader = addReady(player1, new ZhalfirinCrusader());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent destination = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, crusader), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, crusader.getId());
        harness.passBothPriorities();

        assertThat(crusader.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage can be redirected to a player")
    void redirectsDamageToPlayer() {
        Permanent crusader = addReady(player1, new ZhalfirinCrusader());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        int lifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, crusader), null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, crusader.getId());
        harness.passBothPriorities();

        assertThat(crusader.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected; the rest still lands on the Crusader")
    void redirectsOnlyOneDamage() {
        Permanent crusader = addReady(player1, new ZhalfirinCrusader());
        Permanent destination = addReadyStats(player1, 3, 3);
        Permanent attacker = addReadyStats(player2, 2, 2);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, crusader), null, destination.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, crusader), 0)));
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(crusader.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent crusader = addReady(player1, new ZhalfirinCrusader());
        Permanent destination = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, crusader), null, destination.getId());
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
