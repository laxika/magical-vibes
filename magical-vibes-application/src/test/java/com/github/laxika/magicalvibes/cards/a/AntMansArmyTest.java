package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AntMansArmy.class)
class AntMansArmyTest extends BaseCardTest {

    @Test
    void createsFoodTokenWhenChosen() {
        castArmy(0);

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
    }

    @Test
    void createsTreasureTokenWhenChosen() {
        castArmy(1);

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    private void castArmy(int mode) {
        harness.setHand(player1, List.of(new AntMansArmy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
