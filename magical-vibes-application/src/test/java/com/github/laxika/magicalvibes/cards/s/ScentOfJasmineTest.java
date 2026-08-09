package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScentOfJasmineTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each white card in hand")
    void gainsLifeForWhiteCardsInHand() {
        harness.setHand(player1, List.of(
                new ScentOfJasmine(),
                new WhiteKnight(),
                new SerraAngel(),
                new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Gains no life when hand has no white cards")
    void gainsNoLifeWithoutWhiteCards() {
        harness.setHand(player1, List.of(new ScentOfJasmine(), new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
