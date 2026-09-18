package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenWarcraft;
import com.github.laxika.magicalvibes.cards.n.NomadMythmaker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverSeraph.class, NomadMythmaker.class, AvenWarcraft.class})
class SilverSeraphTest extends BaseCardTest {

    @Test
    void thresholdDoesNotBoostWithFewerThanSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new NomadMythmaker());

        assertStats(player1, "Silver Seraph", 6, 6);
        assertStats(player1, "Nomad Mythmaker", 2, 2);
    }

    @Test
    void thresholdBoostsOtherCreaturesYouControl() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new NomadMythmaker());
        harness.addToBattlefield(player2, new NomadMythmaker());

        assertStats(player1, "Silver Seraph", 6, 6);
        assertStats(player1, "Nomad Mythmaker", 4, 4);
        assertStats(player2, "Nomad Mythmaker", 2, 2);
    }

    @Test
    void thresholdUsesItsControllersGraveyardAndUpdatesDynamically() {
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new NomadMythmaker());
        harness.setGraveyard(player2, graveyardWithCards(7));

        assertStats(player1, "Nomad Mythmaker", 2, 2);

        harness.setGraveyard(player1, graveyardWithCards(7));
        assertStats(player1, "Nomad Mythmaker", 4, 4);

        harness.setGraveyard(player1, graveyardWithCards(6));
        assertStats(player1, "Nomad Mythmaker", 2, 2);
    }

    @Test
    void thresholdCountsNoncreatureCardsInGraveyard() {
        harness.setGraveyard(player1, Stream.concat(
                Stream.of(new AvenWarcraft()),
                graveyardWithCards(6).stream()).toList());
        harness.addToBattlefield(player1, new SilverSeraph());
        harness.addToBattlefield(player1, new NomadMythmaker());

        assertStats(player1, "Nomad Mythmaker", 4, 4);
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new NomadMythmaker())
                .toList();
    }

    private void assertStats(Player player, String cardName, int power, int toughness) {
        var creature = findPermanent(player, cardName);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness);
    }
}
