package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoaringSandwing.class, Forest.class, Plains.class})
class SoaringSandwingTest extends BaseCardTest {

    @Test
    @DisplayName("When Soaring Sandwing enters, its controller gains 3 life")
    void gainsLifeWhenItEnters() {
        harness.setHand(player1, List.of(new SoaringSandwing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setLife(player1, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingDiscardsAndOffersPlains() {
        harness.setHand(player1, List.of(new SoaringSandwing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Plains()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soaring Sandwing");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Plains"))
                .hasSize(1);
    }
}
