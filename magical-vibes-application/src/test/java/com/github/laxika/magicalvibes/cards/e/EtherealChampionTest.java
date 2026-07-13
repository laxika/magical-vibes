package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EtherealChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability pays 1 life and adds a 1-damage prevention shield to itself")
    void activationPaysLifeAndAddsShield() {
        Permanent champion = addReadyPermanent(player1, new EtherealChampion());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, indexOf(player1, champion), null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(champion.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The next 1 noncombat damage dealt to the Champion is prevented")
    void preventsNoncombatDamage() {
        Permanent champion = addReadyPermanent(player1, new EtherealChampion());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, champion), null, null);
        harness.passBothPriorities();

        // Pyromancer pings the Champion for 1 — that 1 damage is prevented
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, champion.getId());
        harness.passBothPriorities();

        assertThat(champion.getMarkedDamage()).isEqualTo(0);
        assertThat(champion.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the next 1 damage is prevented; the rest still lands on the Champion")
    void preventsOnlyOneDamage() {
        Permanent champion = addReadyPermanent(player1, new EtherealChampion());
        Permanent attacker = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, champion), null, null);
        harness.passBothPriorities();

        // player2 attacks with a 2/2; the Champion blocks and takes 2 combat damage
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, champion), 0)));
        harness.passBothPriorities();

        // 1 of the 2 combat damage is prevented; 1 remains marked on the Champion
        assertThat(champion.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The prevention shield wears off at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent champion = addReadyPermanent(player1, new EtherealChampion());

        harness.activateAbility(player1, indexOf(player1, champion), null, null);
        harness.passBothPriorities();

        assertThat(champion.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(champion.getDamagePreventionShield()).isEqualTo(0);
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
