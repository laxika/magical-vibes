package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AerialDoombot;
import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KangTheConqueror.class, AerialDoombot.class})
class KangTheConquerorTest extends BaseCardTest {

    private void enableAutoStop() {
        Set<TurnStep> stops = ConcurrentHashMap.newKeySet();
        stops.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops);
        gd.playerAutoStopSteps.put(player2.getId(), stops);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @CardUsed(CaptureOfJingzhou.class)
    void ordinaryExtraTurnDoesNotInheritKangsPowerUpRestriction() {
        enableAutoStop();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent doombot = harness.enterBattlefieldAndReturn(player1, new AerialDoombot());
        harness.enterBattlefieldAndReturn(player1, new KangTheConqueror());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, java.util.List.of(new CaptureOfJingzhou()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        advanceTurn();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(doombot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        advanceTurn();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Power-up abilities can't be activated");
    }

    @Test
    @DisplayName("Power-up gets the entry-turn discount, adds a counter, and grants an extra turn")
    void powerUpIsDiscountedAndGrantsExtraTurn() {
        Permanent kang = harness.enterBattlefieldAndReturn(player1, new KangTheConqueror());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Kang's extra turn prevents Power-up activations until it ends")
    void extraTurnPreventsPowerUps() {
        enableAutoStop();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new AerialDoombot());
        harness.enterBattlefieldAndReturn(player1, new KangTheConqueror());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Power-up abilities can't be activated");

        advanceTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent doombot = findPermanent(player1, "Aerial Doombot");
        assertThat(doombot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
