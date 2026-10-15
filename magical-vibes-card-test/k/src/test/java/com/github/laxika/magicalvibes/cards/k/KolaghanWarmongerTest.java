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

    @Test
    @DisplayName("Declining a Dragon leaves all six cards below the untouched library")
    void mayDeclineDragon() {
        Card dragon = new ShivanDragon();
        List<Card> lookedAt = List.of(dragon, new GrizzlyBears(), new Island(),
                new Shock(), new GrizzlyBears(), new Island());
        Card untouched = new Shock();
        harness.setLibrary(player1, java.util.stream.Stream.concat(
                lookedAt.stream(), java.util.stream.Stream.of(untouched)).toList());
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));
        declareAttack();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(7);
        assertThat(deck.getFirst()).isSameAs(untouched);
        assertThat(deck.subList(1, 7)).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only one of multiple Dragons is taken and the others go below untouched cards")
    void choosesOneOfMultipleDragons() {
        Card chosen = new ShivanDragon();
        Card otherDragon = new ShivanDragon();
        List<Card> rest = List.of(otherDragon, new GrizzlyBears(), new Island(),
                new Shock(), new GrizzlyBears());
        Card untouched = new Island();
        harness.setLibrary(player1, List.of(chosen, rest.get(0), rest.get(1), rest.get(2),
                rest.get(3), rest.get(4), untouched));
        declareAttack();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), otherDragon.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen).doesNotContain(otherDragon);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6);
        assertThat(deck.getFirst()).isSameAs(untouched);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrderElementsOf(rest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Dragon seventh from the top cannot be chosen")
    void doesNotLookBeyondSixCards() {
        Card dragon = new ShivanDragon();
        List<Card> lookedAt = List.of(new GrizzlyBears(), new Island(), new Shock(),
                new GrizzlyBears(), new Island(), new Shock());
        harness.setLibrary(player1, java.util.stream.Stream.concat(
                lookedAt.stream(), java.util.stream.Stream.of(dragon)).toList());
        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(7);
        assertThat(deck.getFirst()).isSameAs(dragon);
        assertThat(deck.subList(1, 7)).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("A library smaller than six still allows choosing a Dragon")
    void shortLibrary() {
        Card dragon = new ShivanDragon();
        Card island = new Island();
        setupTopCards(List.of(dragon, island));
        declareAttack();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves the attack trigger without a choice")
    void emptyLibrary() {
        setupTopCards(List.of());
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));
        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Haste allows attacking immediately and triggers the Dragon choice")
    void attacksWhileSummoningSick() {
        Card dragon = new ShivanDragon();
        setupTopCards(List.of(dragon));
        var warmonger = harness.addToBattlefieldAndReturn(player1, new KolaghanWarmonger());
        warmonger.setSummoningSick(true);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void declareAttack() {
        addCreatureReady(player1, new KolaghanWarmonger());
        declareAttackers(player1, List.of(0));
    }
}
