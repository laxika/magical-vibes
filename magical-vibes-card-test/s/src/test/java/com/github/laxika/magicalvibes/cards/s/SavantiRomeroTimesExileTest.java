package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MakeYourMove;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SavantiRomeroTimesExile.class, MakeYourMove.class})
class SavantiRomeroTimesExileTest extends BaseCardTest {

    @Test
    void putsACounterThenDrawsAndLosesLifeForAllCounters() {
        Permanent savanti = harness.addToBattlefieldAndReturn(player1, new SavantiRomeroTimesExile());
        savanti.setCounterCount(CounterType.CHARGE, 1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        int life = gd.getLife(player1.getId());

        runBeginningOfCombat(player1);

        assertThat(savanti.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(savanti.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(life - 2);
    }

    @Test
    void doesNotTriggerOnOpponentsCombat() {
        Permanent savanti = harness.addToBattlefieldAndReturn(player1, new SavantiRomeroTimesExile());
        int handSize = gd.playerHands.get(player1.getId()).size();
        int life = gd.getLife(player1.getId());

        runBeginningOfCombat(player2);

        assertThat(savanti.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.getLife(player1.getId())).isEqualTo(life);
    }

    @Test
    void usesCountersAtResolutionRatherThanWhenTheAbilityTriggers() {
        Permanent savanti = harness.addToBattlefieldAndReturn(player1, new SavantiRomeroTimesExile());
        int handSize = gd.playerHands.get(player1.getId()).size();
        int life = gd.getLife(player1.getId());

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            startBeginningOfCombat(player1);
            assertThat(gd.stack).hasSize(1);
            savanti.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            savanti.setCounterCount(CounterType.CHARGE, 1);
            resolveAllTriggers();
        });

        assertThat(savanti.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(life - 4);
    }

    @Test
    void drawsAndLosesLifeUsingLastKnownCountersWhenRemovedInResponse() {
        Permanent savanti = harness.addToBattlefieldAndReturn(player1, new SavantiRomeroTimesExile());
        savanti.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        savanti.setCounterCount(CounterType.CHARGE, 1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        int life = gd.getLife(player1.getId());
        harness.setHand(player2, List.of(new MakeYourMove()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            startBeginningOfCombat(player1);
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player2, 0, savanti.getId());
            resolveAllTriggers();
        });

        harness.assertNotOnBattlefield(player1, "Savanti Romero, Time's Exile");
        harness.assertInGraveyard(player1, "Savanti Romero, Time's Exile");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(life - 3);
    }

    private void runBeginningOfCombat(Player activePlayer) {
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            startBeginningOfCombat(activePlayer);
            resolveAllTriggers();
        });
    }

    private void startBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
