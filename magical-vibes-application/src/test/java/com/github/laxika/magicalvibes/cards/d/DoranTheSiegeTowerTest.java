package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DoranTheSiegeTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Doran (0/5) assigns 5 combat damage (its toughness)")
    void doranUsesOwnToughness() {
        Permanent doran = addReadyCreature(player1, new DoranTheSiegeTower());

        assertThat(gqs.getEffectivePower(gd, doran)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, doran)).isEqualTo(5);
        assertThat(gqs.getEffectiveCombatDamage(gd, doran)).isEqualTo(5);
    }

    @Test
    @DisplayName("Controller's creature with higher power assigns toughness")
    void ownCreatureUsesToughness() {
        addReadyCreature(player1, new DoranTheSiegeTower());
        Permanent piker = addReadyCreature(player1, new GoblinPiker()); // 2/1

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's creatures are also affected (global effect)")
    void opponentCreaturesAlsoAffected() {
        addReadyCreature(player1, new DoranTheSiegeTower());
        Permanent opponentPiker = addReadyCreature(player2, new GoblinPiker()); // 2/1
        Permanent opponentSpider = addReadyCreature(player2, new GiantSpider()); // 2/4

        assertThat(gqs.getEffectiveCombatDamage(gd, opponentPiker)).isEqualTo(1); // toughness, not power
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentSpider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature with equal power/toughness is unchanged")
    void equalPowerToughnessUnchanged() {
        addReadyCreature(player1, new DoranTheSiegeTower());
        Permanent bears = addReadyCreature(player2, new GrizzlyBears()); // 2/2

        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's high-power attacker deals toughness damage")
    void opponentAttackerDealsToughnessDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player2, new DoranTheSiegeTower());
        Permanent piker = addReadyCreature(player1, new GoblinPiker()); // 2/1
        piker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19); // 20 - 1 (toughness)
    }

    @Test
    @DisplayName("Effect disappears when Doran leaves the battlefield")
    void effectDisappearsWhenDoranRemoved() {
        Permanent doran = addReadyCreature(player1, new DoranTheSiegeTower());
        Permanent piker = addReadyCreature(player2, new GoblinPiker()); // 2/1

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(1); // toughness

        gd.playerBattlefields.get(player1.getId()).remove(doran);

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2); // back to power
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
