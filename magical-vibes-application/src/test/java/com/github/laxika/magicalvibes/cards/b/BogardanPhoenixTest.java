package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BogardanPhoenix.class)
class BogardanPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("First death: returns to battlefield with a death counter")
    void firstDeathReturnsWithDeathCounter() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new BogardanPhoenix());
        killPhoenix(phoenix);

        Permanent returned = findPermanent(player1, "Bogardan Phoenix");
        assertThat(returned.getCounterCount(CounterType.DEATH)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
    }

    @Test
    @DisplayName("Second death (with death counter): exiles from graveyard")
    void secondDeathExiles() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new BogardanPhoenix());
        phoenix.setCounterCount(CounterType.DEATH, 1);
        var cardId = phoenix.getCard().getId();

        killPhoenix(phoenix);

        harness.assertNotOnBattlefield(player1, "Bogardan Phoenix");
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(cardId));
    }

    @Test
    @DisplayName("Returns under its controller's control when an opponent owns it")
    void stolenPhoenixReturnsUnderItsControllersControl() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player2, new BogardanPhoenix());
        gd.stolenCreatures.put(phoenix.getId(), player1.getId());

        killPhoenix(phoenix);

        harness.assertOnBattlefield(player2, "Bogardan Phoenix");
        harness.assertNotOnBattlefield(player1, "Bogardan Phoenix");
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
        harness.assertNotInGraveyard(player2, "Bogardan Phoenix");
    }

    private void killPhoenix(Permanent phoenix) {
        phoenix.setMarkedDamage(phoenix.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities(); // resolve death trigger
    }
}
