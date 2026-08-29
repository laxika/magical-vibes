package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonWall.class, GrizzlyBears.class})
class DemonWallTest extends BaseCardTest {

    @Test
    @DisplayName("Demon Wall cannot attack while it has no counters")
    void cannotAttackWithoutCounters() {
        Permanent wall = addReadyDemonWall();
        addOpponentBlocker();
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(wall))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Demon Wall can attack with any kind of counter")
    void canAttackWithAnyCounter() {
        Permanent wall = addReadyDemonWall();
        wall.setCounterCount(CounterType.CHARGE, 1);
        addOpponentBlocker();
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of(indexOf(wall)));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Activating Demon Wall's ability puts two +1/+1 counters on it")
    void activationPutsTwoCountersAndAllowsAttacking() {
        Permanent wall = addReadyDemonWall();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, indexOf(wall), null, null);
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        addOpponentBlocker();
        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(indexOf(wall)));

        assertThat(wall.isAttacking()).isTrue();
    }

    private Permanent addReadyDemonWall() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new DemonWall());
        wall.setSummoningSick(false);
        return wall;
    }

    private void addOpponentBlocker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
