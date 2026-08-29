package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CytoplastRootKin.class, GrizzlyBears.class})
class CytoplastRootKinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourCounters() {
        Permanent rootKin = castRootKin();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability adds counters to other countered creatures you control")
    void entersAndAddsCountersToOtherCounteredCreatures() {
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncounteredCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent rootKin = castRootKin();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(counteredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(uncounteredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may move a counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent rootKin = castRootKin();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability moves a counter from a creature you control onto Cytoplast Root-Kin")
    void activatedAbilityMovesCounterOntoSource() {
        Permanent rootKin = castRootKin();
        rootKin.setSummoningSick(false);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(rootKin),
                null,
                bears.getId());
        harness.passBothPriorities();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability cannot target an opponent's creature")
    void activatedAbilityCannotTargetOpponentsCreature() {
        Permanent rootKin = castRootKin();
        rootKin.setSummoningSick(false);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(rootKin),
                null,
                opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castRootKin() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CytoplastRootKin()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Cytoplast Root-Kin");
    }
}
