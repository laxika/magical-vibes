package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NourishingShoalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains X life when cast normally")
    void gainsLifeEqualToX() {
        harness.setHand(player1, List.of(new NourishingShoal()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int lifeBefore = gd.getLife(player1.getId());

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Can be cast by exiling a green card with mana value X")
    void castsByExilingMatchingGreenCard() {
        GrizzlyBears greenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new NourishingShoal(), greenCard));
        int lifeBefore = gd.getLife(player1.getId());

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(greenCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects a card with the wrong mana value or color")
    void alternateCostRequiresMatchingManaValueAndColor() {
        harness.setHand(player1, List.of(new NourishingShoal(), new GrizzlyBears()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, 1))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new NourishingShoal(), new Shock()));
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
