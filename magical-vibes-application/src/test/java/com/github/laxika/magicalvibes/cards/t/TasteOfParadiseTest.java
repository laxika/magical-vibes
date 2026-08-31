package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TasteOfParadise.class})
class TasteOfParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("With no additional payment, controller gains 3 life")
    void gainsThreeLifeWithoutAdditionalPayments() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TasteOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {3}{G}

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Each additional {1}{G} paid gains 3 more life")
    void gainsThreeMoreLifePerAdditionalPayment() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TasteOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 8); // {3}{G} + {1}{G} twice

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{G}", "{1}{G}"), List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 29);
    }

    @Test
    @DisplayName("Additional payments are not free — casting without the mana for them fails")
    void cannotPayAdditionalCostWithoutMana() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TasteOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 4); // only {3}{G}

        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(
                player1, 0, List.of("{1}{G}"), List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
