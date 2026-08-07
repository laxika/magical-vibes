package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClericOfTheForwardOrderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 2 life counting itself")
    void gainsTwoLifeAlone() {
        cast(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("ETB gains 2 life for each copy you control")
    void gainsTwoLifePerCopy() {
        addCreatureReady(player1, new ClericOfTheForwardOrder());
        addCreatureReady(player1, new ClericOfTheForwardOrder());

        cast(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Copies controlled by the opponent are not counted")
    void ignoresOpponentCopies() {
        addCreatureReady(player2, new ClericOfTheForwardOrder());

        cast(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new ClericOfTheForwardOrder()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }
}
