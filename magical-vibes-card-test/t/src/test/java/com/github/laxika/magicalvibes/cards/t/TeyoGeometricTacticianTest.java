package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RingsOfBrighthearth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeyoGeometricTactician.class, Forest.class, GrizzlyBears.class, Island.class, RingsOfBrighthearth.class})
class TeyoGeometricTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 0/4 Wall with defender and flying")
    void etbCreatesFlyingWall() {
        harness.enterBattlefieldAndReturn(player1, new TeyoGeometricTactician());
        harness.passBothPriorities();

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, wall, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("+1 draws for Teyo's controller and a target opponent")
    void plusOneDrawsForBothPlayers() {
        Permanent teyo = addReadyTeyo(player1, 3);
        Card ownDraw = new Forest();
        Card opponentDraw = new Island();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(ownDraw));
        harness.setLibrary(player2, List.of(opponentDraw));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        assertThat(teyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 left restricts attacks to the nearest opponent")
    void minusTwoLeftRestrictsAttackTargets() {
        Permanent teyo = addReadyTeyo(player1, 3);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Left");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(teyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("-2 right restricts attacks to the nearest opponent")
    void minusTwoRightRestrictsAttackTargets() {
        Permanent teyo = addReadyTeyo(player1, 3);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(teyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("+1 cannot target Teyo's controller")
    void plusOneRejectsControllerAsTarget() {
        Permanent teyo = addReadyTeyo(player1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(teyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Right restricts every player relative to their own multiplayer seat")
    void rightRestrictsEachPlayersDefenders() {
        addReadyTeyo(player1, 3);
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentPlaneswalker = harness.addToBattlefieldAndReturn(
                player2, new TeyoGeometricTactician());
        opponentPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");

        UUID player3Id = addThirdSeat();
        assertThat(als.canAttackDefender(gd, ownAttacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, ownAttacker, opponentPlaneswalker.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, ownAttacker, player3Id)).isFalse();
        assertThat(als.canAttackDefender(gd, opponentAttacker, player3Id)).isTrue();
        assertThat(als.canAttackDefender(gd, opponentAttacker, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Left forbids attacks against the other opponent and their planeswalkers")
    void leftRestrictsPlayersAndPlaneswalkers() {
        addReadyTeyo(player1, 3);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentPlaneswalker = harness.addToBattlefieldAndReturn(
                player2, new TeyoGeometricTactician());
        opponentPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Left");

        UUID player3Id = addThirdSeat();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, attacker, opponentPlaneswalker.getId())).isFalse();
    }

    @Test
    @DisplayName("The resolved restriction survives Teyo leaving the battlefield")
    void restrictionSurvivesSourceLeaving() {
        Permanent teyo = addReadyTeyo(player1, 3);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        gd.playerBattlefields.get(player1.getId()).remove(teyo);

        UUID player3Id = addThirdSeat();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    @Test
    @DisplayName("A copied -2 uses the same Teyo's last chosen direction")
    void lastDirectionFromSameTeyoReplacesEarlierChoice() {
        addReadyTeyo(player1, 3);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Left");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");

        UUID player3Id = addThirdSeat();
        assertThat(gd.stack).isEmpty();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    private UUID addThirdSeat() {
        UUID player3Id = UUID.randomUUID();
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        return player3Id;
    }

    private Permanent addReadyTeyo(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TeyoGeometricTactician());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
