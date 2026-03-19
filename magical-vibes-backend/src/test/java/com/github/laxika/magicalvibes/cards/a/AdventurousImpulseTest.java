package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdventurousImpulseTest extends BaseCardTest {

    @Test
    @DisplayName("Adventurous Impulse has correct effect configuration")
    void hasCorrectEffect() {
        AdventurousImpulse card = new AdventurousImpulse();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect.class);
        LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect effect =
                (LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(3);
        assertThat(effect.cardTypes()).containsExactlyInAnyOrder(CardType.CREATURE, CardType.LAND);
    }

    @Test
    @DisplayName("Casting Adventurous Impulse puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Adventurous Impulse");
    }

    @Test
    @DisplayName("Resolves by offering creature and land cards among top three")
    void resolvesOfferingCreaturesAndLands() {
        setupTopCards(List.of(
                new LlanowarElves(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Plains");
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand then orders rest on bottom")
    void choosingCreatureThenOrderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains = new Plains();
        setupTopCards(List.of(elves, shock, plains));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose Llanowar Elves
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(2);
    }

    @Test
    @DisplayName("Choosing a land puts it into hand then orders rest on bottom")
    void choosingLandThenOrderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains = new Plains();
        setupTopCards(List.of(elves, shock, plains));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose Plains (index 1 since only Llanowar Elves and Plains are offered)
        harness.getGameService().handleLibraryCardChosen(gd, player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(2);
    }

    @Test
    @DisplayName("You may choose no card and still reorder all looked cards to bottom")
    void mayChooseNoCard() {
        setupTopCards(List.of(
                new LlanowarElves(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(3);
    }

    @Test
    @DisplayName("If top three has no creature or land cards, directly reorder them to bottom")
    void noMatchingCardsDirectlyReordersBottom() {
        setupTopCards(List.of(
                new Shock(),
                new Shock(),
                new Shock()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(3);
    }

    @Test
    @DisplayName("With empty library, Adventurous Impulse does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Adventurous Impulse goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopCards(List.of(
                new LlanowarElves(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Adventurous Impulse"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With fewer than three cards in library, looks at all available")
    void fewerThanThreeCardsInLibrary() {
        setupTopCards(List.of(
                new LlanowarElves(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new AdventurousImpulse()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Plains");
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
