package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AntMansArmy.class})
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

    @Test
    void tokenTypeIsChosenWhenEnterAbilityResolves() {
        harness.castFromHand(player1, new AntMansArmy(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.handleListChoice(player1, "Create a Treasure token");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void foodCanBeSacrificedImmediatelyForThreeLife() {
        castArmy(0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Food"));

        harness.activateAbility(player1, foodIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void treasureCanBeSacrificedImmediatelyForChosenMana() {
        castArmy(1);
        int treasureIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Treasure"));

        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castArmy(int mode) {
        harness.setHand(player1, List.of(new AntMansArmy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode == 0 ? "Create a Food token" : "Create a Treasure token");
    }
}
