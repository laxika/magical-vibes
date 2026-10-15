package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoranTheSiegeTower.class, GoblinPiker.class, GiantSpider.class, GrizzlyBears.class, Lignify.class})
class DoranTheSiegeTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Doran (0/5) assigns 5 combat damage (its toughness)")
    void doranUsesOwnToughness() {
        Permanent doran = addCreatureReady(player1, new DoranTheSiegeTower());

        assertThat(gqs.getEffectiveCombatDamage(gd, doran)).isEqualTo(5);
    }

    @Test
    @DisplayName("Controller's creature with higher power assigns toughness")
    void ownCreatureUsesToughness() {
        addCreatureReady(player1, new DoranTheSiegeTower());
        Permanent piker = addCreatureReady(player1, new GoblinPiker()); // 2/1

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's creatures are also affected (global effect)")
    void opponentCreaturesAlsoAffected() {
        addCreatureReady(player1, new DoranTheSiegeTower());
        Permanent opponentPiker = addCreatureReady(player2, new GoblinPiker()); // 2/1
        Permanent opponentSpider = addCreatureReady(player2, new GiantSpider()); // 2/4

        assertThat(gqs.getEffectiveCombatDamage(gd, opponentPiker)).isEqualTo(1); // toughness, not power
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentSpider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature with equal power/toughness is unchanged")
    void equalPowerToughnessUnchanged() {
        addCreatureReady(player1, new DoranTheSiegeTower());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's high-power attacker deals toughness damage")
    void opponentAttackerDealsToughnessDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player2, new DoranTheSiegeTower());
        Permanent piker = addCreatureReady(player1, new GoblinPiker()); // 2/1
        piker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19); // 20 - 1 (toughness)
    }

    @Test
    @DisplayName("Effect disappears when Doran leaves the battlefield")
    void effectDisappearsWhenDoranRemoved() {
        Permanent doran = addCreatureReady(player1, new DoranTheSiegeTower());
        Permanent piker = addCreatureReady(player2, new GoblinPiker()); // 2/1

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(1); // toughness

        gd.playerBattlefields.get(player1.getId()).remove(doran);

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2); // back to power
    }

    @Test
    @DisplayName("Doran deals five damage to an unblocked defending player")
    void doranDealsCombatDamage() {
        harness.setLife(player2, 20);
        Permanent doran = addCreatureReady(player1, new DoranTheSiegeTower());
        doran.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Blocking creatures also assign damage using toughness")
    void blockerUsesToughness() {
        addCreatureReady(player2, new DoranTheSiegeTower());
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Lignify removes Doran's global combat assignment ability")
    void losingAbilitiesStopsGlobalEffect() {
        Permanent doran = addCreatureReady(player1, new DoranTheSiegeTower());
        Permanent piker = addCreatureReady(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, doran.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCombatDamage(gd, doran)).isZero();
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
    }
}
