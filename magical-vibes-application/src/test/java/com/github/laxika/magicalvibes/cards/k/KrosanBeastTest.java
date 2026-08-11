package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KrosanBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 with fewer than seven cards in controller's graveyard")
    void noBoostBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addToBattlefield(player1, new KrosanBeast());

        Permanent beast = findBeast();
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +7/+7 with exactly seven cards in controller's graveyard")
    void boostAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new KrosanBeast());

        Permanent beast = findBeast();
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(8);
    }

    @Test
    @DisplayName("Opponent's graveyard does not count")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addToBattlefield(player1, new KrosanBeast());

        Permanent beast = findBeast();
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses the boost when controller's graveyard drops below seven cards")
    void losesBoostWhenGraveyardShrinks() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new KrosanBeast());

        Permanent beast = findBeast();
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(8);

        harness.setGraveyard(player1, graveyardCards(6));
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(1);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }

    private Permanent findBeast() {
        return findPermanent(player1, "Krosan Beast");
    }
}
