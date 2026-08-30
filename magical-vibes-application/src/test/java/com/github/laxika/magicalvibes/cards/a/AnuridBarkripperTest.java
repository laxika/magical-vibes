package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnuridBarkripper.class, Shock.class})
class AnuridBarkripperTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 with seven cards in controller's graveyard")
    void boostAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new AnuridBarkripper());

        Permanent barkripper = findBarkripper();

        assertThat(gqs.getEffectivePower(gd, barkripper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barkripper)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the boost with fewer than seven cards in controller's graveyard")
    void noBoostBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addToBattlefield(player1, new AnuridBarkripper());

        Permanent barkripper = findBarkripper();

        assertThat(gqs.getEffectivePower(gd, barkripper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, barkripper)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's graveyard does not count")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addToBattlefield(player1, new AnuridBarkripper());

        Permanent barkripper = findBarkripper();

        assertThat(gqs.getEffectivePower(gd, barkripper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, barkripper)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses the boost when controller's graveyard drops below seven cards")
    void losesBoostWhenGraveyardShrinks() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new AnuridBarkripper());

        Permanent barkripper = findBarkripper();
        assertThat(gqs.getEffectivePower(gd, barkripper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barkripper)).isEqualTo(4);

        harness.setGraveyard(player1, graveyardCards(6));

        assertThat(gqs.getEffectivePower(gd, barkripper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, barkripper)).isEqualTo(2);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }

    private Permanent findBarkripper() {
        return findPermanent(player1, "Anurid Barkripper");
    }
}
