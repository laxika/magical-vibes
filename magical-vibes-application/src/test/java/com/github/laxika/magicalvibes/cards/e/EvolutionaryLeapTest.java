package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvolutionaryLeapTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and puts the first revealed creature card into hand, rest on the bottom")
    void revealsUntilCreatureAndPutsItIntoHand() {
        harness.addToBattlefield(player1, new EvolutionaryLeap());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(
                new FountainOfYouth(),
                new GrizzlyBears(),
                new EvolutionaryLeap()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(deck).extracting(Card::getName)
                .containsExactlyInAnyOrder("Fountain of Youth", "Evolutionary Leap");
    }

    @Test
    @DisplayName("Puts every revealed card on the bottom when no creature card is found")
    void noCreatureFoundBottomsEverything() {
        harness.addToBattlefield(player1, new EvolutionaryLeap());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new FountainOfYouth(), new EvolutionaryLeap()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Fountain of Youth");
        assertThat(deck).extracting(Card::getName)
                .containsExactlyInAnyOrder("Fountain of Youth", "Evolutionary Leap");
    }
}
