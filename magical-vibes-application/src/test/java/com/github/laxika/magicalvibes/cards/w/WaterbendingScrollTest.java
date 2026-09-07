package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterbendingScroll.class, Island.class, Forest.class})
class WaterbendingScrollTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card with its activation cost reduced by Islands you control")
    void drawsCardWithReducedCost() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new WaterbendingScroll());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(scroll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana after the Island reduction")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new WaterbendingScroll());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not count Islands controlled by an opponent")
    void doesNotCountOpponentsIslands() {
        harness.addToBattlefield(player1, new WaterbendingScroll());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
