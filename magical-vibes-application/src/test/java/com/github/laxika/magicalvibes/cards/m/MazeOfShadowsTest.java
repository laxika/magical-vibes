package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DauthiSlayer;
import com.github.laxika.magicalvibes.cards.s.SearingTouch;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MazeOfShadows.class, DauthiSlayer.class, TrainedArmodon.class, SearingTouch.class})
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
        Permanent attacker = addShadowAttacker(player2, player1);
        attacker.tap();

        activateMaze(attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Prevents combat damage the target creature would deal")
    void preventsCombatDamageDealtByCreature() {
        harness.setLife(player1, 20);
        addReadyMaze(player1);
        Permanent attacker = addShadowAttacker(player2, player1);

        activateMaze(attacker);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the target creature by a blocker")
    void preventsCombatDamageDealtToCreature() {
        addReadyMaze(player1);
        Permanent attacker = addShadowAttacker(player2, player1);
        Permanent blocker = addShadowBlocker(player1);

        activateMaze(attacker);
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Noncombat damage to the target creature is not prevented")
    void doesNotPreventNoncombatDamage() {
        addReadyMaze(player1);
        Permanent attacker = addShadowAttacker(player2, player1);

        activateMaze(attacker);

        harness.setHand(player1, List.of(new SearingTouch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt by other creatures")
    void doesNotPreventOtherCreaturesCombatDamage() {
        harness.setLife(player1, 20);
        addReadyMaze(player1);
        Permanent target = addShadowAttacker(player2, player1);
        addShadowAttacker(player2, player1);

        activateMaze(target);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot target an attacking creature without shadow")
    void cannotTargetNonShadowAttacker() {
        addReadyMaze(player1);
        Permanent attacker = addAttacker(player2, player1, new TrainedArmodon());

        assertThatThrownBy(() -> activateMaze(attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature with shadow that is not attacking")
    void cannotTargetNonAttackingShadowCreature() {
        addReadyMaze(player1);
        Permanent creature = addCreatureReady(player2, new DauthiSlayer());

        assertThatThrownBy(() -> activateMaze(creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing if the target is no longer attacking when the ability resolves")
    void doesNothingIfTargetStopsAttackingBeforeResolution() {
        addReadyMaze(player1);
        Permanent attacker = addShadowAttacker(player2, player1);
        attacker.tap();

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isTrue();
    }

    private void activateMaze(Permanent target) {
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyMaze(Player player) {
        return addCreatureReady(player, new MazeOfShadows());
    }

    private Permanent addShadowAttacker(Player owner, Player defender) {
        return addAttacker(owner, defender, new DauthiSlayer());
    }

    private Permanent addAttacker(Player owner, Player defender, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private Permanent addShadowBlocker(Player owner) {
        Permanent perm = addCreatureReady(owner, new DauthiSlayer());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);
        return perm;
    }
}
