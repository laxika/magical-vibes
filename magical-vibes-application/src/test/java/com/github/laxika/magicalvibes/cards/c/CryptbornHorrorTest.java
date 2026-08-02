package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CryptbornHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with +1/+1 counters equal to life lost by opponents this turn")
    void entersWithCountersEqualToOpponentsLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 5);

        harness.setHand(player1, List.of(new CryptbornHorror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent horror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cryptborn Horror"))
                .findFirst()
                .orElseThrow();
        assertThat(horror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not count life lost by its controller")
    void ignoresControllersLifeLoss() {
        gd.lifeLostThisTurn.put(player1.getId(), 5);

        harness.setHand(player1, List.of(new CryptbornHorror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cryptborn Horror");
    }
}
