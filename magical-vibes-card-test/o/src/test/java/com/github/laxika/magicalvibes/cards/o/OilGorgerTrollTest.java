package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OilGorgerTroll.class})
class OilGorgerTrollTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 3 life and draws a card when you control a permanent with an oil counter")
    void gainsLifeAndDrawsWithOilCounter() {
        Permanent oilPermanent = harness.addToBattlefieldAndReturn(player1, new OilGorgerTroll());
        oilPermanent.setCounterCount(CounterType.OIL, 1);
        prepareCast();

        resolveOilGorgerTroll();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB gains 3 life but does not draw without an oil counter")
    void gainsLifeWithoutDrawingWithoutOilCounter() {
        prepareCast();

        resolveOilGorgerTroll();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's oil counter does not enable the draw")
    void opponentOilCounterDoesNotEnableDraw() {
        Permanent oilPermanent = harness.addToBattlefieldAndReturn(player2, new OilGorgerTroll());
        oilPermanent.setCounterCount(CounterType.OIL, 1);
        prepareCast();

        resolveOilGorgerTroll();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The draw condition is checked when the ETB resolves")
    void doesNotDrawIfOilCounterIsRemovedBeforeResolution() {
        Permanent oilPermanent = harness.addToBattlefieldAndReturn(player1, new OilGorgerTroll());
        oilPermanent.setCounterCount(CounterType.OIL, 1);
        prepareCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(oilPermanent);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An oil counter gained after the trigger enables the draw")
    void drawsIfOilCounterIsAddedBeforeResolution() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new OilGorgerTroll());
        prepareCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        permanent.setCounterCount(CounterType.OIL, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The entering Troll's own oil counter enables the draw")
    void drawsWithOilCounterOnItself() {
        prepareCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().setCounterCount(CounterType.OIL, 3);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A counter of another type does not enable the draw")
    void otherCounterTypeDoesNotEnableDraw() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new OilGorgerTroll());
        permanent.setCounterCount(CounterType.CHARGE, 1);
        prepareCast();

        resolveOilGorgerTroll();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing the last oil counter before resolution prevents the draw")
    void doesNotDrawAfterLastOilCounterIsRemoved() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new OilGorgerTroll());
        permanent.setCounterCount(CounterType.OIL, 1);
        prepareCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        permanent.setCounterCount(CounterType.OIL, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new OilGorgerTroll()));
        harness.setLibrary(player1, List.of(new OilGorgerTroll()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void resolveOilGorgerTroll() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
