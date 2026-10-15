package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
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

@CardUsed({CrossbonesMaliciousMercenary.class, DocOcksHenchmen.class, GrizzlyBears.class})
class CrossbonesMaliciousMercenaryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on itself and deals 2 damage to each opponent for a Villain entering")
    void triggersForVillainEntering() {
        Permanent crossbones = harness.addToBattlefieldAndReturn(player1, new CrossbonesMaliciousMercenary());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not trigger for a non-Villain creature")
    void doesNotTriggerForNonVillain() {
        Permanent crossbones = harness.addToBattlefieldAndReturn(player1, new CrossbonesMaliciousMercenary());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent crossbones = harness.addToBattlefieldAndReturn(player1, new CrossbonesMaliciousMercenary());
        harness.setHand(player1, List.of(new DocOcksHenchmen(), new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for its own entry or consume its trigger allowance")
    void doesNotTriggerForItself() {
        Permanent crossbones = harness.enterBattlefieldAndReturn(player1, new CrossbonesMaliciousMercenary());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();

        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opposing Villain does not trigger or consume the trigger allowance")
    void doesNotTriggerForOpposingVillain() {
        Permanent crossbones = harness.addToBattlefieldAndReturn(player1, new CrossbonesMaliciousMercenary());
        harness.enterBattlefieldAndReturn(player2, new DocOcksHenchmen());

        assertThat(gd.stack).isEmpty();
        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();

        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A non-Villain does not consume the trigger allowance")
    void nonVillainDoesNotConsumeTriggerAllowance() {
        Permanent crossbones = harness.addToBattlefieldAndReturn(player1, new CrossbonesMaliciousMercenary());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();

        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The once-per-turn limit applies before the first trigger resolves")
    void secondVillainBeforeResolutionDoesNotTriggerAgain() {
        Permanent crossbones = harness.addToBattlefieldAndReturn(player1, new CrossbonesMaliciousMercenary());
        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can trigger again during the opponent's next turn")
    void triggersAgainOnNextTurn() {
        Permanent crossbones = harness.addToBattlefieldAndReturn(player1, new CrossbonesMaliciousMercenary());
        harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();

        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(crossbones.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }
}
