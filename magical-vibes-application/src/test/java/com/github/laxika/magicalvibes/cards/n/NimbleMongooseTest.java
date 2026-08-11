package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NimbleMongooseTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 1/1 with fewer than seven cards in its controller's graveyard")
    void remainsBaseSizeBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addToBattlefield(player1, new NimbleMongoose());

        Permanent mongoose = findPermanent(player1, "Nimble Mongoose");
        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 at seven cards in its controller's graveyard")
    void getsBoostAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new NimbleMongoose());

        Permanent mongoose = findPermanent(player1, "Nimble Mongoose");
        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only its controller's graveyard")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addToBattlefield(player1, new NimbleMongoose());

        Permanent mongoose = findPermanent(player1, "Nimble Mongoose");
        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses the boost when its controller's graveyard drops below seven cards")
    void losesBoostBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new NimbleMongoose());

        Permanent mongoose = findPermanent(player1, "Nimble Mongoose");
        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).removeLast();

        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(1);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
