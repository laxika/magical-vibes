package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JessieZaneFangbringer.class, AmbushViper.class, GrizzlyBears.class})
class JessieZaneFangbringerTest extends BaseCardTest {

    @Test
    void entersAndConjuresAnAmbushViperIntoTheTopSix() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        castJessie();

        List<Card> library = gd.playerDecks.get(player1.getId());
        int conjuredIndex = findAmbushViperIndex(library);
        assertThat(library).hasSize(8);
        assertThat(conjuredIndex).isBetween(0, 5);
    }

    @Test
    void castingASnakeConjuresAnotherAmbushViper() {
        addCreatureReady(player1, new JessieZaneFangbringer());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AmbushViper()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(Card::getName, "Ambush Viper")
                .hasSize(1);
    }

    @Test
    void conjuredAmbushViperDrawsWhenItEnters() {
        harness.setLibrary(player1, List.of());
        castJessie();

        Card conjuredViper = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> "Ambush Viper".equals(card.getName()))
                .findFirst()
                .orElseThrow();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(conjuredViper));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    private void castJessie() {
        harness.setHand(player1, List.of(new JessieZaneFangbringer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private int findAmbushViperIndex(List<Card> library) {
        for (int i = 0; i < library.size(); i++) {
            if (library.get(i) instanceof AmbushViper) {
                return i;
            }
        }
        return -1;
    }
}
