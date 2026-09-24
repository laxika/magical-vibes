package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnerFire.class, InnerChamberGuard.class})
class InnerFireTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana equal to the number of cards remaining in hand")
    void addsManaForCardsRemainingInHand() {
        harness.setHand(player1, List.of(new InnerFire(), new InnerChamberGuard(), new InnerChamberGuard()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no mana when no cards remain in hand")
    void addsNoManaWithEmptyHand() {
        harness.setHand(player1, List.of(new InnerFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
