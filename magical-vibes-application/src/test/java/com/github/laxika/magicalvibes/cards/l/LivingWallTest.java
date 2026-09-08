package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingWall.class, CrawWurm.class})
class LivingWallTest extends BaseCardTest {

    @Test
    void resolvingRegenerationAbilityGrantsShield() {
        addLivingWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Living Wall").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesLivingWallFromLethalCombatDamage() {
        Permanent wall = addLivingWallReady(player1);
        wall.setRegenerationShield(1);
        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        addAttackingCrawWurm(player2);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Living Wall");
        Permanent survivingWall = findPermanent(player1, "Living Wall");
        assertThat(survivingWall.isTapped()).isTrue();
        assertThat(survivingWall.getRegenerationShield()).isZero();
    }

    @Test
    void livingWallDiesFromLethalCombatDamageWithoutShield() {
        Permanent wall = addLivingWallReady(player1);
        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        addAttackingCrawWurm(player2);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Living Wall");
        harness.assertInGraveyard(player1, "Living Wall");
    }

    private Permanent addLivingWallReady(Player player) {
        return addCreatureReady(player, new LivingWall());
    }

    private void addAttackingCrawWurm(Player player) {
        Permanent perm = addCreatureReady(player, new CrawWurm());
        perm.setAttacking(true);
    }
}
