package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.n.NobleElephant;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({Yare.class, NobleElephant.class, Plains.class})
class YareTest extends BaseCardTest {

    @Test
    @DisplayName("Yare boosts a creature the defending player controls and grants two additional blocks")
    void boostsAndGrantsAdditionalBlocks() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        addAttacker();

        castYare(player2, blocker);

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getToughnessModifier()).isZero();
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosted creature can block three attackers")
    void boostedCreatureCanBlockThreeAttackers() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        addAttacker();
        addAttacker();
        addAttacker();

        castYare(player2, blocker);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        ));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Boosted creature cannot block more than three attackers")
    void boostedCreatureCannotBlockFourAttackers() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        addAttacker();
        addAttacker();
        addAttacker();
        addAttacker();

        castYare(player2, blocker);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2),
                new BlockerAssignment(blockerIdx, 3)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Attacking player can cast Yare on a creature controlled by the defending player")
    void attackingPlayerCanCastYareOnDefendingCreature() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        addAttacker();

        castYare(player1, blocker);

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost and additional-block grant wear off at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
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
        addCreatureReady(player2, new NobleElephant());
        Permanent attacker = addAttacker();

        harness.setHand(player2, List.of(new Yare()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature defending player controls");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        addAttacker();

        harness.setHand(player2, List.of(new Yare()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature defending player controls");
    }

    @Test
    @DisplayName("Cannot be cast outside combat when nobody is being attacked")
    void cannotBeCastWithoutADefendingPlayer() {
        Permanent creature = addCreatureReady(player2, new NobleElephant());

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

    private Permanent addAttacker() {
        Permanent atk = addCreatureReady(player1, new NobleElephant());
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        return atk;
    }
}
