package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverSeraph.class, GrizzlyBears.class})
class SilverSeraphTest extends BaseCardTest {

    @Test
    void thresholdDoesNotBoostWithFewerThanSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertStats(player1, "Silver Seraph", 6, 6);
        assertStats(player1, "Grizzly Bears", 2, 2);
    }

    @Test
    void thresholdBoostsOtherCreaturesYouControl() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertStats(player1, "Silver Seraph", 6, 6);
        assertStats(player1, "Grizzly Bears", 4, 4);
        assertStats(player2, "Grizzly Bears", 2, 2);
    }

    @Test
    void thresholdUsesItsControllersGraveyardAndUpdatesDynamically() {
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player2, graveyardWithCards(7));

        assertStats(player1, "Grizzly Bears", 2, 2);

        harness.setGraveyard(player1, graveyardWithCards(7));
        assertStats(player1, "Grizzly Bears", 4, 4);

        harness.setGraveyard(player1, graveyardWithCards(6));
        assertStats(player1, "Grizzly Bears", 2, 2);
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }

    private void assertStats(Player player, String cardName, int power, int toughness) {
        var creature = findPermanent(player, cardName);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness);
    }
}
