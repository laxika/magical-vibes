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

        assertThat(gd.regenerationShieldCount(findPermanent(player1, "Living Wall").getId())).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesLivingWallFromLethalCombatDamage() {
        Permanent wall = addLivingWallReady(player1);
        gd.addRegenerationShields(wall.getId(), 1);
        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        addAttackingCrawWurm(player2);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Living Wall");
        Permanent survivingWall = findPermanent(player1, "Living Wall");
        assertThat(survivingWall.isTapped()).isTrue();
        assertThat(gd.regenerationShieldCount(survivingWall.getId())).isZero();
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

    @Test
    void creatingShieldDoesNotImmediatelyRegenerateOrRemoveDamage() {
        Permanent wall = addLivingWallReady(player1);
        wall.setMarkedDamage(3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.regenerationShieldCount(wall.getId())).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.regenerationShieldCount(wall.getId())).isEqualTo(1);
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    void tappedSummoningSickWallCanRegenerateUsingColoredMana() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new LivingWall());
        wall.setSummoningSick(true);
        wall.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.regenerationShieldCount(wall.getId())).isEqualTo(1);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    void activatedShieldSavesWallAndRemovesDamageAndCombatAssignments() {
        Permanent wall = addLivingWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        addAttackingCrawWurm(player2);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Living Wall");
        harness.assertNotInGraveyard(player1, "Living Wall");
        assertThat(wall.isTapped()).isTrue();
        assertThat(gd.regenerationShieldCount(wall.getId())).isZero();
        assertThat(wall.getMarkedDamage()).isZero();
        assertThat(wall.isBlocking()).isFalse();
        assertThat(wall.getBlockingTargets()).isEmpty();
    }
    private Permanent addLivingWallReady(Player player) {
        return addCreatureReady(player, new LivingWall());
    }

    private void addAttackingCrawWurm(Player player) {
        Permanent perm = addCreatureReady(player, new CrawWurm());
        perm.setAttacking(true);
    }
}
