package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(findPermanentOrNull(player1, "Bogardan Phoenix")).isNull();
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(cardId));
    }

    private void killPhoenix(Permanent phoenix) {
        phoenix.setMarkedDamage(phoenix.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities(); // resolve death trigger
    }

    private Permanent findPermanentOrNull(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
