package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptorOfCurrents.class, GrizzlyBears.class, Forest.class})
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
        resolveAllTriggers();

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

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId(), thirdConvokeCreature.getId()));

        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();
        assertThat(thirdConvokeCreature.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The optional target can be declined")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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

    @Test
    @DisplayName("Another copy controlled by you is a legal target")
    void returnsAnotherCopyYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DisruptorOfCurrents());
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()))
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target.getCard());
    }

    @Test
    @DisplayName("The optional target can be declined even when a legal target exists")
    void declinesAvailableTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DisruptorOfCurrents());
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blue creatures can convoke the colored mana despite summoning sickness")
    void convokesBlueManaWithNewCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DisruptorOfCurrents());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DisruptorOfCurrents());
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A permanent controlled by an opponent returns to its actual owner's hand")
    void returnsStolenPermanentToOwner() {
        harness.setHand(player2, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DisruptorOfCurrents());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.setHand(player1, List.of(new DisruptorOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The entering creature cannot choose itself but can choose another copy")
    void cannotTargetItselfOnEntering() {
        harness.setHand(player2, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DisruptorOfCurrents());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new DisruptorOfCurrents());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(target.getCard());
    }
}
