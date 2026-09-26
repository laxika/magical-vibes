package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FarmerCotton.class)
class FarmerCottonTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X Halflings and X Food tokens")
    void createsXHalflingsAndFood() {
        cast(2);

        List<Permanent> halflings = findPermanents(player1, "Halfling").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        List<Permanent> food = findPermanents(player1, "Food");

        assertThat(halflings).hasSize(2);
        assertThat(halflings).allSatisfy(halfling -> {
            assertThat(halfling.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(halfling.getCard().getSubtypes()).contains(CardSubtype.HALFLING);
            assertThat(halfling.getEffectivePower()).isEqualTo(1);
            assertThat(halfling.getEffectiveToughness()).isEqualTo(1);
        });
        assertThat(food).hasSize(2);
        assertThat(food).allSatisfy(token -> {
            assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        });
    }

    @Test
    @DisplayName("With X=0, creates no tokens")
    void xZeroCreatesNoTokens() {
        cast(0);

        assertThat(findPermanents(player1, "Halfling")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    private void cast(int xValue) {
        harness.setHand(player1, List.of(new FarmerCotton()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        harness.castCreature(player1, 0, xValue);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
