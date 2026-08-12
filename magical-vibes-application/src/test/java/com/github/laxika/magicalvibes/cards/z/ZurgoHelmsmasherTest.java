package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZurgoHelmsmasherTest extends BaseCardTest {

    @Test
    @DisplayName("Zurgo must attack each combat when able")
    void mustAttackWhenAble() {
        addReadyZurgo(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Zurgo has indestructible during its controller's turn only")
    void indestructibleDuringControllerTurnOnly() {
        Permanent zurgo = addReadyZurgo(player1);
        zurgo.setMarkedDamage(2);

        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zurgo);

        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zurgo);
    }

    @Test
    @DisplayName("Zurgo gets a +1/+1 counter when a creature it damaged dies")
    void gainsCounterWhenDamagedCreatureDies() {
        Permanent zurgo = addReadyZurgo(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zurgo);
        assertThat(zurgo.getMarkedDamage()).isEqualTo(2);
        assertThat(zurgo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyZurgo(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new ZurgoHelmsmasher());
    }
}
