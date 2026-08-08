package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LaviniaOfTheTenthTest extends BaseCardTest {

    @Test
    @DisplayName("Detains an opponent's cheap creature so it can't attack")
    void detainsCheapOpponentCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLavinia();

        assertThatThrownBy(() -> declareAttack(bear))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained permanent's activated abilities can't be activated")
    void detainedPermanentCantActivateAbilities() {
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        castLavinia();

        assertThatThrownBy(() -> harness.tapPermanent(player2, indexOf(player2, elves)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Permanents with mana value 5 or more are unaffected")
    void expensivePermanentsAreUnaffected() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castLavinia();

        assertThatCode(() -> declareAttack(angel)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Your own permanents are not detained")
    void ownPermanentsAreNotDetained() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(false);

        castLavinia();

        assertThatCode(() -> harness.tapPermanent(player1, indexOf(player1, elves)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Detain wears off at Lavinia's controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLavinia();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bear)).doesNotThrowAnyException();
    }

    private void castLavinia() {
        harness.setHand(player1, List.of(new LaviniaOfTheTenth()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(indexOf(player2, creature)));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
