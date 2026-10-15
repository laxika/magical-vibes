package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({VampireScrivener.class, AngelOfMercy.class, Shock.class})
class VampireScrivenerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains a +1/+1 counter when its controller gains life during their turn")
    void gainsCounterOnControllerLifeGainDuringTheirTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new VampireScrivener());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent scrivener = findPermanent(player1, "Vampire Scrivener");
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains a +1/+1 counter when its controller loses life during their turn")
    void gainsCounterOnControllerLifeLossDuringTheirTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new VampireScrivener());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        Permanent scrivener = findPermanent(player1, "Vampire Scrivener");
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for life loss during an opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new VampireScrivener());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        Permanent scrivener = findPermanent(player1, "Vampire Scrivener");
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for life gain during an opponent's turn")
    void doesNotTriggerOnLifeGainDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent scrivener = harness.addToBattlefieldAndReturn(player1, new VampireScrivener());

        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.stack).isEmpty();
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers separately for each life-loss event, not each point of life")
    void triggersForEachLifeLossEvent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent scrivener = harness.addToBattlefieldAndReturn(player1, new VampireScrivener());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers separately for each life-gain event during its controller's turn")
    void triggersForEachLifeGainEvent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent scrivener = harness.addToBattlefieldAndReturn(player1, new VampireScrivener());

        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life during the controller's turn")
    void doesNotTriggerForOpponentsLifeGain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent scrivener = harness.addToBattlefieldAndReturn(player1, new VampireScrivener());

        harness.enterBattlefieldAndReturn(player2, new AngelOfMercy());
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent loses life during the controller's turn")
    void doesNotTriggerForOpponentsLifeLoss() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent scrivener = harness.addToBattlefieldAndReturn(player1, new VampireScrivener());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(scrivener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
