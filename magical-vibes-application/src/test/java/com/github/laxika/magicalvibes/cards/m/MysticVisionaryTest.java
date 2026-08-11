package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MysticVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have flying without threshold")
    void noFlyingWithoutThreshold() {
        harness.addToBattlefield(player1, new MysticVisionary());

        assertThat(gqs.hasKeyword(gd, findVisionary(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying with seven cards in its controller's graveyard")
    void thresholdGrantsFlying() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticVisionary());

        assertThat(gqs.hasKeyword(gd, findVisionary(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Six cards are not enough for threshold")
    void sixCardsAreNotEnough() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addToBattlefield(player1, new MysticVisionary());

        assertThat(gqs.hasKeyword(gd, findVisionary(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticVisionary());

        assertThat(gqs.hasKeyword(gd, findVisionary(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses flying when its controller's graveyard drops below seven cards")
    void losesFlyingWhenGraveyardShrinks() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticVisionary());
        Permanent visionary = findVisionary();
        assertThat(gqs.hasKeyword(gd, visionary, Keyword.FLYING)).isTrue();

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.hasKeyword(gd, visionary, Keyword.FLYING)).isFalse();
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }

    private Permanent findVisionary() {
        return findPermanent(player1, "Mystic Visionary");
    }
}
