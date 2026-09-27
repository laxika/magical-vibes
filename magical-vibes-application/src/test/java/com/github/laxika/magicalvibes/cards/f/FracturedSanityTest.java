package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FracturedSanity.class, GrizzlyBears.class})
class FracturedSanityTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent mills fourteen cards")
    void eachOpponentMillsFourteenCards() {
        harness.setLibrary(player1, libraryOfSize(20));
        harness.setLibrary(player2, libraryOfSize(20));
        harness.setHand(player1, List.of(new FracturedSanity()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(14);
    }

    @Test
    @DisplayName("Cycling mills each opponent four cards and draws a card")
    void cyclingMillsEachOpponentAndDraws() {
        harness.setLibrary(player1, libraryOfSize(1));
        harness.setLibrary(player2, libraryOfSize(10));
        harness.setHand(player1, List.of(new FracturedSanity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fractured Sanity");
    }

    private List<Card> libraryOfSize(int size) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            library.add(new GrizzlyBears());
        }
        return library;
    }
}
