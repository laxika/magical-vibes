package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.t.Terrarion;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({GiveNoGround.class, FieldCreeper.class, Terrarion.class})
class GiveNoGroundTest extends BaseCardTest {

    @Test
    @DisplayName("Give No Ground boosts the target creature")
    void boostsTargetCreature() {
        Permanent target = addCreature(player2);
        castGiveNoGround(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("Target creature can block any number of attackers")
    void targetCreatureCanBlockAnyNumberOfAttackers() {
        Permanent blocker = addCreature(player2);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        addAttacker();
        addAttacker();
        addAttacker();

        castGiveNoGround(blocker);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1),
                new BlockerAssignment(blockerIndex, 2)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Give No Ground's effects expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addCreature(player2);
        castGiveNoGround(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    @Test
    @DisplayName("Give No Ground cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Terrarion());
        harness.setHand(player1, List.of(new GiveNoGround()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Give No Ground can target your creature without affecting other creatures")
    void canTargetOwnCreature() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player1);

        castGiveNoGround(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(6);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    @Test
    @DisplayName("Repeated Give No Ground boosts stack and still permit multiple blocks")
    void repeatedCastsStillAllowMultipleBlocks() {
        Permanent blocker = addCreature(player2);
        addAttacker();
        addAttacker();
        castGiveNoGround(blocker);
        castGiveNoGround(blocker);

        assertThat(blocker.getPowerModifier()).isEqualTo(4);
        assertThat(blocker.getToughnessModifier()).isEqualTo(12);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Give No Ground does not let a tapped creature block")
    void tappedCreatureStillCannotBlock() {
        Permanent blocker = addCreature(player2);
        addAttacker();
        castGiveNoGround(blocker);
        blocker.setTapped(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Give No Ground has no effect when its target leaves before resolution")
    void removedTargetReceivesNeitherEffect() {
        Permanent target = addCreature(player2);
        Permanent other = addCreature(player2);
        harness.setHand(player1, List.of(new GiveNoGround()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getAdditionalBlocksUntilEndOfTurn()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.getAdditionalBlocksUntilEndOfTurn()).isZero();
        harness.assertInGraveyard(player1, "Give No Ground");
        assertThat(gd.stack).isEmpty();
    }

    private void castGiveNoGround(Permanent target) {
        harness.setHand(player1, List.of(new GiveNoGround()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new FieldCreeper());
    }

    private void addAttacker() {
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
    }
}
