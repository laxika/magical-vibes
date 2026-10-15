package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.ExperimentalAugury;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindspliceApparatus.class, Divination.class, GrizzlyBears.class, ExperimentalAugury.class})
class MindspliceApparatusTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an oil counter on itself at the beginning of your upkeep")
    void addsOilCounterAtUpkeep() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(apparatus.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reduces your instant and sorcery spells by the number of oil counters")
    void reducesInstantAndSorcerySpells() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        apparatus.setCounterCount(CounterType.OIL, 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce creature spells")
    void doesNotReduceCreatureSpells() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        apparatus.setCounterCount(CounterType.OIL, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(apparatus.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void reducesInstantSpells() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        apparatus.setCounterCount(CounterType.OIL, 1);
        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void excessOilDoesNotReduceColoredMana() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        apparatus.setCounterCount(CounterType.OIL, 5);
        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceOpponentsSpells() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        apparatus.setCounterCount(CounterType.OIL, 2);
        harness.setHand(player2, List.of(new ExperimentalAugury()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void otherCounterTypesDoNotReduceCosts() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        apparatus.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionsFromMultipleApparatusesAddTogether() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        first.setCounterCount(CounterType.OIL, 1);
        second.setCounterCount(CounterType.OIL, 1);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canBeCastDuringOpponentsTurnWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.setHand(player1, List.of(new MindspliceApparatus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mindsplice Apparatus");
    }

    @Test
    void noOilCountersMeansNoReduction() {
        harness.addToBattlefield(player1, new MindspliceApparatus());
        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
