package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeeperOfTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when activated targeting an opponent with at least two more cards")
    void drawsCard() {
        readyKeeper(1, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The hand-size condition is checked only when activating")
    void handSizeConditionIsCheckedOnlyOnActivation() {
        readyKeeper(1, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate when the opponent has only one more card")
    void cannotActivateWithoutTwoMoreCards() {
        readyKeeper(1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        readyKeeper(1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyKeeper(int controllerHandSize, int opponentHandSize) {
        harness.setHand(player1, cards(controllerHandSize));
        harness.setHand(player2, cards(opponentHandSize));
        addCreatureReady(player1, new KeeperOfTheMind());
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
