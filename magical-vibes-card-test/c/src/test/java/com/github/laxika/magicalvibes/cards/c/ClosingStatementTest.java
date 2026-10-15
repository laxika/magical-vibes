package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ClosingStatement.class, GrizzlyBears.class, GarrukWildspeaker.class})
class ClosingStatementTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opposing creature and puts a counter on your creature")
    void destroysOpponentAndAddsCounterToOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForReducedCast();

        harness.castInstant(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys a planeswalker and allows omitting the counter target")
    void destroysPlaneswalkerWithoutCounterTarget() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        prepareForReducedCast();

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Does not receive the cost reduction outside your end step")
    void costReductionOnlyDuringYourEndStep() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClosingStatement()));
        addReducedCostMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature you control for destruction")
    void cannotDestroyOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClosingStatement()));
        addReducedCostMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }

    @Test
    void noReductionDuringOpponentsEndStep() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForReducedCast();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastAtFullCostOutsideOwnEndStep() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForReducedCast();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotPutCounterOnOpponentsCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForReducedCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterStillResolvesWhenDestructionTargetLeaves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForReducedCast();
        harness.castInstant(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);

        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void destructionStillResolvesWhenCounterTargetLeaves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForReducedCast();
        harness.castInstant(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void omittedCounterTargetDoesNotReceiveCounterAfterRegeneration() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setRegenerationShield(1);
        prepareForReducedCast();

        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void prepareForReducedCast() {
        harness.setHand(player1, List.of(new ClosingStatement()));
        addReducedCostMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
    }

    private void addReducedCostMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
