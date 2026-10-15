package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
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

@CardUsed({Yare.class, NobleElephant.class, Plains.class, Boomerang.class})
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

    @Test
    @DisplayName("Yare can be cast at the beginning of combat before attackers are declared")
    void canBeCastBeforeAttackersAreDeclared() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        castYare(player2, blocker);

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Yare can target the defending player's creature at end of combat without attackers")
    void canBeCastAtEndOfCombatWithoutAttackers() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        castYare(player2, blocker);

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the last attacker in response does not invalidate Yare's target")
    void stillResolvesAfterLastAttackerLeavesCombat() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        Permanent attacker = addAttacker();
        harness.setHand(player2, List.of(new Yare()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castInstant(player2, 0, blocker.getId());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Yare does not affect a target that leaves the battlefield before resolution")
    void doesNotAffectRemovedTarget() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        addAttacker();
        harness.setHand(player2, List.of(new Yare()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castInstant(player2, 0, blocker.getId());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card instanceof Yare);
    }

    @Test
    @DisplayName("Two Yares stack their power boosts and additional blocking permissions")
    void twoCopiesAllowBlockingFiveAttackers() {
        Permanent blocker = addCreatureReady(player2, new NobleElephant());
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        for (int i = 0; i < 5; i++) {
            addAttacker();
        }

        castYare(player2, blocker);
        castYare(player2, blocker);

        assertThat(blocker.getPowerModifier()).isEqualTo(6);
        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(4);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2),
                new BlockerAssignment(blockerIdx, 3),
                new BlockerAssignment(blockerIdx, 4)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2, 3, 4);
    }

    private void castYare(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Yare()));
        harness.addMana(caster, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private Permanent addAttacker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent atk = addCreatureReady(player1, new NobleElephant());
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        return atk;
    }
}
