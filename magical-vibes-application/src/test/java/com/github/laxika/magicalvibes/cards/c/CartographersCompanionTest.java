package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CartographersCompanion.class})
class CartographersCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Map token")
    void etbCreatesMapToken() {
        harness.setHand(player1, List.of(new CartographersCompanion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).singleElement()
                .satisfies(map -> assertThat(map.getCard().hasType(CardType.ARTIFACT)).isTrue())
                .satisfies(map -> assertThat(map.getCard().getSubtypes()).contains(CardSubtype.MAP));
    }
}
