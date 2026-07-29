package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class YareTest extends BaseCardTest {

    @Test
    @DisplayName("Yare boosts a creature the defending player controls and grants two additional blocks")
    void boostsAndGrantsAdditionalBlocks() {
        Permanent blocker = addCreature(player2);
        addAttacker();

        castYare(player2, blocker);

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getToughnessModifier()).isZero();
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosted creature can block three attackers")
    void boostedCreatureCanBlockThreeAttackers() {
        Permanent blocker = addCreature(player2);
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        addAttacker();
        addAttacker();
        addAttacker();

        castYare(player2, blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        ));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Boost and additional-block grant wear off at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent blocker = addCreature(player2);
        addAttacker();
        castYare(player2, blocker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature the attacking player controls")
    void cannotTargetAttackingPlayersCreature() {
        addCreature(player2);
        Permanent attacker = addAttacker();

        harness.setHand(player2, List.of(new Yare()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature defending player controls");
    }

    @Test
    @DisplayName("Cannot be cast outside combat when nobody is being attacked")
    void cannotBeCastWithoutADefendingPlayer() {
        Permanent creature = addCreature(player2);

        harness.setHand(player2, List.of(new Yare()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castYare(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Yare()));
        harness.addMana(caster, ManaColor.WHITE, 3);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttacker() {
        Permanent atk = new Permanent(new GrizzlyBears());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(atk);
        return atk;
    }
}
