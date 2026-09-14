package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BargainingTable.class, Forest.class})
class BargainingTableTest extends BaseCardTest {

    @Test
    @DisplayName("Pays the opponent's hand size and draws a card")
    void paysOpponentHandSizeAndDraws() {
        Permanent table = addTable();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(table.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Cannot activate without enough mana for the opponent's hand size")
    void cannotPayOpponentHandSize() {
        Permanent table = addTable();
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(table.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate for zero mana when the opponent's hand is empty")
    void activatesForZeroWhenOpponentHandIsEmpty() {
        Permanent table = addTable();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(table.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    private Permanent addTable() {
        Permanent table = harness.addToBattlefieldAndReturn(player1, new BargainingTable());
        table.setSummoningSick(false);
        return table;
    }
}
