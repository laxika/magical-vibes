package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EerieInterference.class, HillGiant.class, ProdigalPyromancer.class, Shock.class})
class EerieInterferenceTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents creature damage to you")
    void preventsCreatureDamageToController() {
        harness.setLife(player1, 20);
        Permanent pyromancer = addReadyPyromancer(player2);
        castEerieInterference();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, battlefieldIndex(player2, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents creature damage to a creature you control")
    void preventsCreatureDamageToControlledCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent pyromancer = addReadyPyromancer(player2);
        castEerieInterference();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, battlefieldIndex(player2, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent creature damage to an opponent")
    void doesNotPreventCreatureDamageToOpponent() {
        harness.setLife(player2, 20);
        Permanent pyromancer = addReadyPyromancer(player1);
        castEerieInterference();

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, battlefieldIndex(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent noncreature damage to you or your creatures")
    void doesNotPreventNoncreatureDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        castEerieInterference();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevention wears off at end of turn")
    void preventionWearsOffAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent pyromancer = addReadyPyromancer(player2);
        castEerieInterference();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, battlefieldIndex(player2, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void preventsDamageToCreatureEnteringAfterResolution() {
        Permanent pyromancer = addReadyPyromancer(player2);
        castEerieInterference();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, battlefieldIndex(player2, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotPreventDamageToOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent pyromancer = addReadyPyromancer(player1);
        castEerieInterference();

        harness.activateAbility(player1, battlefieldIndex(player1, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotPreventNoncreatureDamageToController() {
        harness.setLife(player1, 20);
        castEerieInterference();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void preventsCombatDamageToController() {
        harness.setLife(player1, 20);
        castEerieInterference();
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void preventsCombatDamageToControlledBlockerButNotOpposingAttacker() {
        castEerieInterference();
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new HillGiant());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void preventsCreatureDamageAfterSourceDiesInResponse() {
        harness.setLife(player1, 20);
        Permanent pyromancer = addReadyPyromancer(player2);
        castEerieInterference();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, battlefieldIndex(player2, pyromancer), null, player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pyromancer.getId());
        harness.assertInGraveyard(player2, "Prodigal Pyromancer");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void castEerieInterference() {
        harness.setHand(player1, List.of(new EerieInterference()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0);
    }

    private Permanent addReadyPyromancer(Player player) {
        return addCreatureReady(player, new ProdigalPyromancer());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
