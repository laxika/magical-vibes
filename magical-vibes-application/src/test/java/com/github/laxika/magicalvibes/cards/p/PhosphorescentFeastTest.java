package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhosphorescentFeastTest extends BaseCardTest {

    private int life(Player player) {
        return harness.getGameData().playerLifeTotals.get(player.getId());
    }

    private void pay(Player player) {
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Gains 2 life for each green mana symbol among the cards left in hand")
    void gainsTwoLifePerGreenSymbol() {
        pay(player1);
        // Feast at index 0 leaves hand on cast; the two Grizzly Bears ({1}{G} each) stay behind.
        harness.setHand(player1, List.of(new PhosphorescentFeast(), new GrizzlyBears(), new GrizzlyBears()));
        int before = life(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(life(player1)).isEqualTo(before + 4);
    }

    @Test
    @DisplayName("Cards with no green mana symbols contribute no life")
    void noGreenSymbolsGainsZero() {
        pay(player1);
        harness.setHand(player1, List.of(new PhosphorescentFeast(), new Shock()));
        int before = life(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(life(player1)).isEqualTo(before);
    }
}
