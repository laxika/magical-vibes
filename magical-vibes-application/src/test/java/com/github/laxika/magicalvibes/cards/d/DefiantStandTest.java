package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefiantStand.class, GrizzlyBears.class, Forest.class})
class DefiantStandTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers while attacked: target gets +1/+3 and untaps")
    void boostsAndUntapsWhenAttacked() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent blocker = tappedCreature(player2);
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0, blocker.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(3);
        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        attacker.tap();
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(3);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent target = tappedCreature(player2);
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);

        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent target = tappedCreature(player2);
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot cast during declare attackers if not attacked")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        // Attacker aims at nobody the caster controls.
        addAttackerTargeting(player1, player1);
        Permanent target = tappedCreature(player2);
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent target = tappedCreature(player2);
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender) {
        Permanent perm = addCreatureReady(attackerController, new GrizzlyBears());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent tappedCreature(Player player) {
        Permanent perm = addCreatureReady(player, new GrizzlyBears());
        perm.tap();
        return perm;
    }
}
