package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MazeOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one colorless mana")
    void tapsForColorless() {
        addReadyMaze(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps the target attacking creature with shadow")
    void untapsTargetShadowAttacker() {
        addReadyMaze(player1);
        Permanent attacker = addShadowAttacker(player2, player1, 2, 2);
        attacker.tap();

        activateMaze(attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Prevents combat damage the target creature would deal")
    void preventsCombatDamageDealtByCreature() {
        harness.setLife(player1, 20);
        addReadyMaze(player1);
        Permanent attacker = addShadowAttacker(player2, player1, 2, 2);

        activateMaze(attacker);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the target creature by a blocker")
    void preventsCombatDamageDealtToCreature() {
        addReadyMaze(player1);
        Permanent attacker = addShadowAttacker(player2, player1, 2, 2);
        addBlocker(player1, 3, 3);

        activateMaze(attacker);
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("Noncombat damage to the target creature is not prevented")
    void doesNotPreventNoncombatDamage() {
        addReadyMaze(player1);
        Permanent attacker = addShadowAttacker(player2, player1, 2, 2);

        activateMaze(attacker);

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(attacker.getId());
        assertThat(gd.creaturesWithAllDamagePrevented).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target an attacking creature without shadow")
    void cannotTargetNonShadowAttacker() {
        addReadyMaze(player1);
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        assertThatThrownBy(() -> activateMaze(attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateMaze(Permanent target) {
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyMaze(Player player) {
        Permanent perm = new Permanent(new MazeOfShadows());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addShadowAttacker(Player owner, Player defender, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        bears.setKeywords(Set.of(Keyword.SHADOW));
        return addAttacker(owner, defender, bears);
    }

    private Permanent addAttacker(Player owner, Player defender, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private Permanent addBlocker(Player owner, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = new Permanent(bears);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }
}
