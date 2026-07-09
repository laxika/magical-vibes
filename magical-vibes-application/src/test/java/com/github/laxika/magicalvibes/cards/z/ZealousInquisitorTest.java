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

class ZealousInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability registers an amount-limited, any-source redirect shield protecting itself")
    void activationCreatesShield() {
        Permanent inquisitor = addReadyPermanent(player1, new ZealousInquisitor());
        Permanent destination = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        var shield = gd.creatureDamageRedirectShields.getFirst();
        assertThat(shield.protectedPermanentId()).isEqualTo(inquisitor.getId());
        assertThat(shield.damageSourceId()).isNull();
        assertThat(shield.remainingAmount()).isEqualTo(1);
        assertThat(shield.redirectTargetId()).isEqualTo(destination.getId());
    }

    @Test
    @DisplayName("Noncombat damage to Zealous Inquisitor is redirected to the target creature")
    void redirectsNoncombatDamage() {
        Permanent inquisitor = addReadyPermanent(player1, new ZealousInquisitor());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent destination = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());
        harness.passBothPriorities();

        // Pyromancer pings the Inquisitor for 1 — that 1 damage is dealt to the destination instead
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, inquisitor.getId());
        harness.passBothPriorities();

        assertThat(inquisitor.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected; the rest still lands on the Inquisitor")
    void redirectsOnlyOneDamage() {
        Permanent inquisitor = addReadyPermanent(player1, new ZealousInquisitor());
        Permanent destination = addReadyStats(player1, 3, 3);
        Permanent attacker = addReadyStats(player2, 2, 2);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());
        harness.passBothPriorities();

        // player2 attacks with a 2/2; the Inquisitor blocks and takes 2 combat damage
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, inquisitor), 0)));
        harness.passBothPriorities();

        // 1 of the 2 combat damage is redirected to the destination; 1 remains on the Inquisitor
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(inquisitor.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent inquisitor = addReadyPermanent(player1, new ZealousInquisitor());
        Permanent destination = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
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
