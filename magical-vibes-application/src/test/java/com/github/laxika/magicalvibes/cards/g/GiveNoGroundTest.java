package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
        addCreature(player2);
        Permanent noncreature = new Permanent(new com.github.laxika.magicalvibes.cards.p.Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(noncreature);
        harness.setHand(player1, List.of(new GiveNoGround()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castGiveNoGround(Permanent target) {
        harness.setHand(player1, List.of(new GiveNoGround()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addAttacker() {
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
    }
}
