package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KolaghanWarmonger.class, GrizzlyBears.class, Island.class, ShivanDragon.class, Shock.class})
class KolaghanWarmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger offers only Dragon cards among the top six")
    void attackTriggerOffersDragons() {
        Card dragon = new ShivanDragon();
        setupTopCards(List.of(dragon, new GrizzlyBears(), new Island(), new Shock(),
                new GrizzlyBears(), new Island()));
        declareAttack();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactly(dragon.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chosen Dragon goes to hand and the rest go to the library bottom")
    void chosenDragonGoesToHand() {
        Card dragon = new ShivanDragon();
        setupTopCards(List.of(dragon, new GrizzlyBears(), new Island(), new Shock(),
                new GrizzlyBears(), new Island()));
        declareAttack();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).doesNotContain(dragon);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No Dragon among the top six creates no choice")
    void noDragonNeedsNoChoice() {
        setupTopCards(List.of(new GrizzlyBears(), new Island(), new Shock(),
                new GrizzlyBears(), new Island(), new Shock()));
        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void declareAttack() {
        addCreatureReady(player1, new KolaghanWarmonger());
        declareAttackers(player1, List.of(0));
    }
}
