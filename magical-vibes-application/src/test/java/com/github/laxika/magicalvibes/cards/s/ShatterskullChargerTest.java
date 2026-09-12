package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(ShatterskullCharger.class)
class ShatterskullChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, returns to its owner's hand at its controller's end step")
    void returnsWithoutKicker() {
        harness.setHand(player1, List.of(new ShatterskullCharger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        assertThat(findCharger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Shatterskull Charger");
        harness.assertInHand(player1, "Shatterskull Charger");
    }

    @Test
    @DisplayName("With kicker, enters with a +1/+1 counter and stays on the battlefield")
    void kickedChargerStaysOnBattlefield() {
        harness.setHand(player1, List.of(new ShatterskullCharger()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        assertThat(findCharger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        harness.assertNotInHand(player1, "Shatterskull Charger");
    }

    private Permanent findCharger() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Shatterskull Charger"))
                .findFirst()
                .orElseThrow();
    }
}
