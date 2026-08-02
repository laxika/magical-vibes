package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JaliraMasterPolymorphistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature and puts the first nonlegendary creature revealed onto the battlefield")
    void polymorphsAnotherCreature() {
        addCreatureReady(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(
                new JaliraMasterPolymorphist(),
                new FountainOfYouth(),
                new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(deck).extracting(Card::getName)
                .containsExactlyInAnyOrder("Jalira, Master Polymorphist", "Fountain of Youth");
    }

    @Test
    @DisplayName("Puts all revealed cards on the bottom when no nonlegendary creature is found")
    void noMatchingCreatureReturnsRevealedCardsToLibrary() {
        addCreatureReady(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new JaliraMasterPolymorphist(), new FountainOfYouth()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(deck).extracting(Card::getName)
                .containsExactlyInAnyOrder("Jalira, Master Polymorphist", "Fountain of Youth");
    }
}
