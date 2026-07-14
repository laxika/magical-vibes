package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GreedTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B} and 2 life draws a card")
    void payingManaAndLifeDrawsACard() {
        harness.addToBattlefield(player1, new Greed());
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can activate repeatedly, paying 2 life each time")
    void canActivateRepeatedly() {
        harness.addToBattlefield(player1, new Greed());
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot activate without {B} available")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new Greed());
        setDeck(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        harness.addToBattlefield(player1, new Greed());
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
