package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ShadowOfMortality.class)
class ShadowOfMortalityTest extends BaseCardTest {

    @Test
    void doesNotReduceCostAtStartingLife() {
        harness.setHand(player1, List.of(new ShadowOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void reducesGenericCostByMissingLife() {
        harness.setLife(player1, 14);
        harness.setHand(player1, List.of(new ShadowOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
    }

    @Test
    void reductionCannotBecomeNegativeAboveStartingLife() {
        harness.setLife(player1, 21);
        harness.setHand(player1, List.of(new ShadowOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
