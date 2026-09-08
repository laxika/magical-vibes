package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DazzlingLights;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThoughtboundPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever its controller surveils, it gets a +1/+1 counter")
    void surveilingAddsCounter() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        harness.passBothPriorities();

        assertThat(phantasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("It cannot attack with fewer than three +1/+1 counters")
    void cannotAttackBelowThreshold() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        phantasm.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("It can attack with three or more +1/+1 counters despite defender")
    void canAttackAtThreshold() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        phantasm.setSummoningSick(false);
        phantasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());
        int phantasmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(phantasm);
        beginAttackers();

        gs.declareAttackers(gd, player1, List.of(phantasmIndex));

        assertThat(phantasm.isAttacking()).isTrue();
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }
}
