package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Oneirophage.class, CounselOfTheSoratami.class})
class OneirophageTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on Oneirophage")
    void triggersOnDrawStepDraw() {
        Permanent oneirophage = harness.addToBattlefieldAndReturn(player1, new Oneirophage());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(oneirophage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Drawing multiple cards puts one +1/+1 counter on Oneirophage per card")
    void triggersOncePerCardDrawn() {
        Permanent oneirophage = harness.addToBattlefieldAndReturn(player1, new Oneirophage());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(oneirophage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Oneirophage")
    void doesNotTriggerOnOpponentDraw() {
        Permanent oneirophage = harness.addToBattlefieldAndReturn(player1, new Oneirophage());

        advanceToDraw(player2);

        assertThat(oneirophage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter is added only when the draw trigger resolves")
    void counterWaitsForTriggerResolution() {
        Permanent oneirophage = harness.addToBattlefieldAndReturn(player1, new Oneirophage());

        advanceToDraw(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(oneirophage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        assertThat(oneirophage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Oneirophage gets its own counter when its controller draws")
    void multipleCopiesTriggerIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Oneirophage());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Oneirophage());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Oneirophage());

        advanceToDraw(player1);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A draw trigger cannot put counters on a new object after its source leaves")
    void departedSourceDoesNotPutCounterOnNewPermanent() {
        Oneirophage card = new Oneirophage();
        Permanent original = harness.addToBattlefieldAndReturn(player1, card);

        advanceToDraw(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
