package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TidingsOfWar.class)
class TidingsOfWarTest extends BaseCardTest {

    @Test
    void normalCastAmassesGoblinsOne() {
        harness.setHand(player1, List.of(new TidingsOfWar()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void flashbackAmassesGoblinsThreeAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new TidingsOfWar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Tidings of War");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tidings of War"));
    }
}
