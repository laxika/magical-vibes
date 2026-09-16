package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenWarcraft;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverSeraph.class, SuntailHawk.class, AvenWarcraft.class})
class SilverSeraphTest extends BaseCardTest {

    @Test
    void thresholdDoesNotBoostWithFewerThanSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new SuntailHawk());

        assertStats(player1, "Silver Seraph", 6, 6);
        assertStats(player1, "Suntail Hawk", 1, 1);
    }

    @Test
    void thresholdBoostsOtherCreaturesYouControl() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());

        assertStats(player1, "Silver Seraph", 6, 6);
        assertStats(player1, "Suntail Hawk", 3, 3);
        assertStats(player2, "Suntail Hawk", 1, 1);
    }

    @Test
    void thresholdUsesItsControllersGraveyardAndUpdatesDynamically() {
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setGraveyard(player2, graveyardWithCards(7));

        assertStats(player1, "Suntail Hawk", 1, 1);

        harness.setGraveyard(player1, graveyardWithCards(7));
        assertStats(player1, "Suntail Hawk", 3, 3);

        harness.setGraveyard(player1, graveyardWithCards(6));
        assertStats(player1, "Suntail Hawk", 1, 1);
    }

    @Test
    void thresholdCountsNoncreatureCardsInGraveyard() {
        List<Card> graveyard = new ArrayList<>(graveyardWithCards(6));
        graveyard.add(new AvenWarcraft());
        harness.setGraveyard(player1, graveyard);
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new SuntailHawk());

        assertStats(player1, "Suntail Hawk", 3, 3);
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new SuntailHawk())
                .toList();
    }

    private void assertStats(Player player, String cardName, int power, int toughness) {
        var creature = findPermanent(player, cardName);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness);
    }
}
