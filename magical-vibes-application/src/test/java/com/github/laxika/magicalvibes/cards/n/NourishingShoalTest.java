package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.l.LoamDweller;
import com.github.laxika.magicalvibes.cards.m.MendingHands;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NourishingShoal.class, LoamDweller.class, GnarledMass.class, MendingHands.class})
class NourishingShoalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains X life when cast normally")
    void gainsLifeEqualToX() {
        harness.setHand(player1, List.of(new NourishingShoal()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Can be cast by exiling a green card with mana value X")
    void castsByExilingMatchingGreenCard() {
        LoamDweller greenCard = new LoamDweller();
        harness.setHand(player1, List.of(new NourishingShoal(), greenCard));
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstantWithAlternateExileFromHand(player1, 0, 2, null, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(greenCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects a card with the wrong mana value or color")
    void alternateCostRequiresMatchingManaValueAndColor() {
        harness.setHand(player1, List.of(new NourishingShoal(), new GnarledMass()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, 2, null, 1))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new NourishingShoal(), new MendingHands()));
        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, 1, null, 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
