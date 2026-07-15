package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TowerGeistTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Tower Geist puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tower Geist");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tower Geist"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tower Geist");
    }

    @Test
    @DisplayName("ETB with two cards in library enters reveal choice state")
    void etbEntersRevealChoiceState() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing a card puts it in hand and the other into graveyard")
    void choosingPutsOneInHandRestInGraveyard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        setupTopCards(List.of(card0, card1));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card0);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing clears awaiting state")
    void choosingClearsAwaitingState() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        setupTopCards(List.of(card0, card1));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card in library, it automatically goes to hand")
    void oneCardInLibrary() {
        gd.playerDecks.get(player1.getId()).clear();
        Card singleCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(singleCard);
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("With empty library, nothing happens")
    void emptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAndResolveEtb() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
