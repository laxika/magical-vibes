package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineSoothsayer.class, SanguineBond.class, GrizzlyBears.class})
class SanguineSoothsayerTest extends BaseCardTest {

    @Test
    void attackConjuresSanguineBondIntoTopFifteenCards() {
        harness.setLibrary(player1, cards(15));
        addCreatureReady(player1, new SanguineSoothsayer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(16);
        assertThat(library.subList(0, 15)).filteredOn(Card::getName, "Sanguine Bond").hasSize(1);
        assertThat(library.subList(15, 16)).allMatch(card -> !"Sanguine Bond".equals(card.getName()));
        assertThat(library.stream()
                .filter(card -> "Sanguine Bond".equals(card.getName()))
                .allMatch(card -> player1.getId().equals(card.getOwnerId())))
                .isTrue();
    }

    @Test
    void conjuredBondCanBeCastForFreeAndDrawsOnEntry() {
        harness.setLibrary(player1, cards(15));
        addCreatureReady(player1, new SanguineSoothsayer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Card conjuredBond = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> "Sanguine Bond".equals(card.getName()))
                .findFirst()
                .orElseThrow();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(conjuredBond));
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Sanguine Bond")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
