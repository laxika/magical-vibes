package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnchovyBananaPizza;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourierOfComestibles.class, AnchovyBananaPizza.class})
class CourierOfComestiblesTest extends BaseCardTest {

    @Test
    @DisplayName("Finds a Food card and puts it into hand")
    void findsFoodCard() {
        castCourier(List.of(new AnchovyBananaPizza(), new CourierOfComestibles()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Anchovy & Banana Pizza");
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when no Food card is found")
    void createsFoodTokenWhenNoFoodCardIsFound() {
        castCourier(List.of(new CourierOfComestibles()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when the search is declined")
    void createsFoodTokenWhenSearchIsDeclined() {
        castCourier(List.of(new AnchovyBananaPizza()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when no card is chosen from the search")
    void createsFoodTokenWhenNoCardIsChosen() {
        castCourier(List.of(new AnchovyBananaPizza()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates one Food token when the searched library is empty")
    void createsFoodTokenWithEmptyLibrary() {
        castCourier(List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Food"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("New Food token can be tapped and sacrificed for three life")
    void foodTokenCanBeUsedImmediately() {
        castCourier(List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        var battlefield = gd.playerBattlefields.get(player1.getId());
        int foodIndex = battlefield.indexOf(battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Food"))
                .findFirst().orElseThrow());
        harness.activateAbility(player1, foodIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 10);
        harness.assertOnBattlefield(player1, "Courier of Comestibles");
    }

    private void castCourier(List<Card> library) {
        harness.setHand(player1, List.of(new CourierOfComestibles()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }
}
