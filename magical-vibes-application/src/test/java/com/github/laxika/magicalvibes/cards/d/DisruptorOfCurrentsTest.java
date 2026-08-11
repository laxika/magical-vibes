package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisruptorOfCurrentsTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows casting during an opponent's turn")
    void castsWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        gs.passPriority(gd, player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(DisruptorOfCurrents.class);
    }

    @Test
    @DisplayName("ETB returns the chosen nonland permanent to its owner's hand")
    void etbReturnsTargetToOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof DisruptorOfCurrents);
    }

    @Test
    @DisplayName("Convoke lets creatures help cast Disruptor of Currents")
    void castsWithConvoke() {
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, target.getId(), null, List.of(),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId(), thirdConvokeCreature.getId()));

        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();
        assertThat(thirdConvokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The optional target can be declined")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof DisruptorOfCurrents);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land cannot be chosen as the target")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }
}
