package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlainWalker.class, GrizzlyBears.class, Plains.class, Panopticon.class, JaceBeleren.class})
class PlainWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked while the defending player controls a planeswalker")
    void cannotBeBlockedByPlaneswalkerwalk() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addPlaneswalker(player2, 4);
        Permanent attacker = addCreatureReady(player1, new PlainWalker());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Cannot be blocked while the defending player controls a Plains")
    void cannotBeBlockedByPlainswalk() {
        harness.addToBattlefield(player2, new Plains());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new PlainWalker());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when the defending player controls no planeswalker")
    void canBeBlockedWithoutPlaneswalker() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new PlainWalker());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The attacking player's planeswalker does not prevent blocking")
    void ownPlaneswalkerDoesNotPreventBlocking() {
        addPlaneswalker(player1, 3);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new PlainWalker());
        declareAttackersAndPrepareBlockers(
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Planeswalks after dealing combat damage to a player")
    void planeswalksAfterCombatDamageToPlayer() {
        preparePlanechase();
        Permanent attacker = addCreatureReady(player1, new PlainWalker());
        var oldPlaneId = gd.planechase.faceUp.getFirst().getId();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.planechase.faceUp).singleElement()
                .extracting(PlanarObject::getId)
                .isNotEqualTo(oldPlaneId);
    }

    @Test
    @DisplayName("Planeswalks after dealing combat damage to a planeswalker")
    void planeswalksAfterCombatDamageToPlaneswalker() {
        preparePlanechase();
        Permanent attacker = addCreatureReady(player1, new PlainWalker());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        attacker.setAttackTarget(planeswalker.getId());
        attacker.setAttacking(true);
        var oldPlaneId = gd.planechase.faceUp.getFirst().getId();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.planechase.faceUp).singleElement()
                .extracting(PlanarObject::getId)
                .isNotEqualTo(oldPlaneId);
    }

    @Test
    @DisplayName("Combat damage to a creature does not cause a planeswalk")
    void doesNotPlaneswalkAfterBlockedCombat() {
        preparePlanechase();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new PlainWalker());
        var oldPlaneId = gd.planechase.faceUp.getFirst().getId();

        declareAttackersAndPrepareBlockers(
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.planechase.faceUp).singleElement()
                .extracting(PlanarObject::getId)
                .isEqualTo(oldPlaneId);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Planeswalking has no effect outside Planechase")
    void combatDamageWithoutPlanechase() {
        Permanent attacker = addCreatureReady(player1, new PlainWalker());
        int oldLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(oldLife - 2);
        assertThat(gd.planechase).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void preparePlanechase() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        gd.planechase.faceUp.add(new PlanarObject(new Panopticon(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new JaceBeleren());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
