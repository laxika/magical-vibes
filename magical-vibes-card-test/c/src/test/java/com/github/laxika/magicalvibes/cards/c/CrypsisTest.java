package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Crypsis.class, GrizzlyBears.class, HillGiant.class, ProdigalPyromancer.class, TurnToFrog.class})
class CrypsisTest extends BaseCardTest {

    @Test
    @DisplayName("Crypsis untaps the target and protects it from opponent creatures")
    void untapsAndProtectsFromOpponentCreatures() {
        Permanent target = addTappedCreature(player1);
        castResolve(target);

        assertThat(target.isTapped()).isFalse();

        target.setAttacking(true);
        addCreatureReady(player2, new HillGiant());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from opponent creatures expires at end of turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player1);
        castResolve(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        target.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Crypsis can target only a creature you control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Crypsis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Protection prevents combat damage without preventing the protected creature from blocking")
    void protectedCreatureCanBlockAndSurvivesCombat() {
        Permanent target = addTappedCreature(player1);
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        castResolve(target);

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Protection forbids targeting by an opponent creature's activated ability")
    void opponentCreatureCannotTargetProtectedCreature() {
        Permanent target = addTappedCreature(player1);
        addCreatureReady(player2, new ProdigalPyromancer());
        castResolve(target);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection does not prevent targeting or damage from a creature you control")
    void friendlyCreatureCanTargetAndDamageProtectedCreature() {
        Permanent target = addTappedCreature(player1);
        addCreatureReady(player1, new ProdigalPyromancer());
        castResolve(target);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Gaining protection makes an opponent creature's pending targeted ability illegal")
    void pendingOpponentAbilityLosesItsTarget() {
        Permanent target = addTappedCreature(player1);
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        gs.passPriority(gd, player2);

        castResolve(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Crypsis grants protection after an earlier effect removed all abilities")
    void protectionGrantedAfterAbilityRemovalStillApplies() {
        Permanent target = addTappedCreature(player1);
        addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        castResolve(target);
        assertThat(target.isTapped()).isFalse();
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.tap();
        return creature;
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new Crypsis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
