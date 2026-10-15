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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KangTheConqueror.class, AerialDoombot.class, CaptureOfJingzhou.class})
class KangTheConquerorTest extends BaseCardTest {

    private void advanceTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
        });
    }

    @Test
    void ordinaryExtraTurnDoesNotInheritKangsPowerUpRestriction() {
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
        harness.castAndResolveSorcery(player1, 0, 0);
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
    @Test
    void powerUpCannotBeActivatedAgainEvenBeforeItResolves() {
        harness.enterBattlefieldAndReturn(player1, new KangTheConqueror());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void powerUpRequiresFullCostWhenKangDidNotEnterThisTurn() {
        Permanent kang = harness.addToBattlefieldAndReturn(player1, new KangTheConqueror());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void extraTurnAlsoPreventsOpponentsPowerUps() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new KangTheConqueror());
        harness.enterBattlefieldAndReturn(player2, new AerialDoombot());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Power-up abilities can't be activated");
    }
}
