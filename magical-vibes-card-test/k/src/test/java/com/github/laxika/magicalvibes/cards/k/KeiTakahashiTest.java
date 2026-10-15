package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeiTakahashi.class, GrizzlyBears.class, ProdigalPyromancer.class})
class KeiTakahashiTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage dealt to the target creature")
    void preventsNextTwoDamageToTargetCreature() {
        addReadyKei();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activateKei(target);

        addReadyPyromancer(player2);
        addReadyPyromancer(player2);
        addReadyPyromancer(player2);

        dealDamage(player2, 0, target);
        dealDamage(player2, 1, target);
        assertThat(target.getMarkedDamage()).isZero();

        dealDamage(player2, 2, target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        addReadyKei();
        addReadyPyromancer(player2);
        addReadyPyromancer(player2);
        addReadyPyromancer(player2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activateKei(target);

        dealDamage(player2, 0, target);
        dealDamage(player2, 1, target);
        assertThat(target.getMarkedDamage()).isZero();

        dealDamage(player2, 2, target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        addReadyKei();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activateKei(target);
        addReadyPyromancer(player2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        dealDamage(player2, 0, target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addReadyKei();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can prevent damage to itself")
    void canTargetItself() {
        Permanent kei = addCreatureReady(player1, new KeiTakahashi());
        addReadyPyromancer(player2);
        addReadyPyromancer(player2);
        addReadyPyromancer(player2);

        activateKei(kei);
        dealDamage(player2, 0, kei);
        dealDamage(player2, 1, kei);
        assertThat(kei.getMarkedDamage()).isZero();
        dealDamage(player2, 2, kei);
        assertThat(kei.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Kei Takahashi");
    }

    @Test
    @DisplayName("Ability still resolves after Kei Takahashi dies")
    void abilityResolvesAfterSourceDies() {
        Permanent kei = addCreatureReady(player1, new KeiTakahashi());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addReadyPyromancer(player2);
        addReadyPyromancer(player2);
        addReadyPyromancer(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        dealDamage(player2, 0, kei);
        dealDamage(player2, 1, kei);
        harness.assertInGraveyard(player1, "Kei Takahashi");
        harness.passBothPriorities();

        dealDamage(player2, 2, target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage without preventing the target's own damage")
    void preventsCombatDamageToTargetOnly() {
        addReadyKei();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        activateKei(target);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activation taps Kei Takahashi and prevents activating again while tapped")
    void activationRequiresUntappedSource() {
        Permanent kei = addCreatureReady(player1, new KeiTakahashi());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activateKei(target);

        assertThat(kei.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent kei = addCreatureReady(player1, new KeiTakahashi());
        kei.setSummoningSick(true);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kei.isTapped()).isFalse();
    }

    private void addReadyKei() {
        addCreatureReady(player1, new KeiTakahashi());
    }

    private void addReadyPyromancer(Player player) {
        addCreatureReady(player, new ProdigalPyromancer());
    }

    private void activateKei(Permanent target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void dealDamage(Player player, int permanentIndex, Permanent target) {
        harness.activateAbility(player, permanentIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
