package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeyoGeometricTactician.class, Forest.class, GrizzlyBears.class, Island.class})
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

    private Permanent addReadyTeyo(Player player, int loyalty) {
        Permanent perm = new Permanent(new TeyoGeometricTactician());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
