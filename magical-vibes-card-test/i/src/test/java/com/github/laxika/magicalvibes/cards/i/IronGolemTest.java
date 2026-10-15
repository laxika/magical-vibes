package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirCultElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronGolem.class, AirCultElemental.class})
class IronGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Iron Golem must attack when it is able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new IronGolem());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Iron Golem must block when it is able")
    void mustBlockWhenAble() {
        Permanent golem = addCreatureReady(player2, new IronGolem());
        Permanent attacker = addCreatureReady(player1, new IronGolem());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        int golemIndex = gd.playerBattlefields.get(player2.getId()).indexOf(golem);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(golemIndex, attackerIndex))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Attacking with vigilance leaves Iron Golem untapped")
    void attackingDoesNotTapGolem() {
        Permanent golem = addCreatureReady(player1, new IronGolem());
        addCreatureReady(player2, new IronGolem());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(golem.isAttacking()).isTrue();
        assertThat(golem.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Summoning sickness prevents the attack requirement from forcing an attack")
    void summoningSickGolemMayStayBack() {
        Permanent golem = addCreatureReady(player1, new IronGolem());
        golem.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
        assertThat(golem.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A tapped Iron Golem is not required to attack")
    void tappedGolemMayStayBack() {
        Permanent golem = addCreatureReady(player1, new IronGolem());
        golem.tap();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
        assertThat(golem.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A tapped Iron Golem is not required to block")
    void tappedGolemMayDeclineBlock() {
        Permanent golem = addCreatureReady(player2, new IronGolem());
        golem.tap();
        Permanent attacker = addCreatureReady(player1, new IronGolem());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Iron Golem need not block when the only attacker has flying")
    void cannotBlockFlyingAttacker() {
        addCreatureReady(player2, new IronGolem());
        Permanent attacker = addCreatureReady(player1, new AirCultElemental());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Summoning sickness does not excuse Iron Golem from blocking")
    void summoningSickGolemMustBlock() {
        Permanent golem = addCreatureReady(player2, new IronGolem());
        golem.setSummoningSick(true);
        Permanent attacker = addCreatureReady(player1, new IronGolem());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
