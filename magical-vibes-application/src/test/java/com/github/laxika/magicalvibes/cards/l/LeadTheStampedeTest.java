package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeadTheStampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Lead the Stampede has correct effect structure")
    void hasCorrectProperties() {
        LeadTheStampede card = new LeadTheStampede();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(5);
        assertThat(effect.predicate()).isEqualTo(new CardTypePredicate(CardType.CREATURE));
        assertThat(effect.anyNumber()).isTrue();
    }

    @Test
    @DisplayName("Casting Lead the Stampede puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Lead the Stampede");
    }

    @Test
    @DisplayName("Resolves by offering multi-select of creature cards among top five")
    void resolvesOfferingMultiSelectOfCreatures() {
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
    }

    @Test
    @DisplayName("Choosing multiple creature cards puts them all into hand then reorders rest")
    void choosingMultipleCreaturesThenReorderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        setupTopFive(List.of(elves, shock, bears, plains, swamp));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose both creature cards
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId(), bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(3);

        List<Card> remaining = gd.interaction.libraryView().reorderCards();
        int iShock = indexOf(remaining, "Shock");
        int iPlains = indexOf(remaining, "Plains");
        int iSwamp = indexOf(remaining, "Swamp");
        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(iShock, iPlains, iSwamp));

        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Shock", "Plains", "Swamp");
    }

    @Test
    @DisplayName("Choosing a single creature card puts it into hand then reorders rest")
    void choosingSingleCreatureThenReorderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        setupTopFive(List.of(elves, shock, bears, plains, swamp));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose only one creature
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .doesNotContain("Grizzly Bears");
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(4);
    }

    @Test
    @DisplayName("You may choose no creature cards and still reorder all looked cards to bottom")
    void mayChooseNoCreature() {
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        // Choose no creatures (empty list)
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    @Test
    @DisplayName("If top five has no creature cards, directly reorder them to bottom")
    void noCreaturesDirectlyReordersBottom() {
        setupTopFive(List.of(
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    @Test
    @DisplayName("With empty library, Lead the Stampede does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Lead the Stampede goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lead the Stampede"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All five creature cards can be selected")
    void allFiveCreaturesCanBeSelected() {
        LlanowarElves elves1 = new LlanowarElves();
        LlanowarElves elves2 = new LlanowarElves();
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        setupTopFive(List.of(elves1, elves2, bears1, bears2, bears3));
        harness.setHand(player1, List.of(new LeadTheStampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose all five creature cards
        harness.handleMultipleCardsChosen(player1,
                List.of(elves1.getId(), elves2.getId(), bears1.getId(), bears2.getId(), bears3.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        // No remaining cards to reorder - should auto-resolve
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    private void setupTopFive(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private int indexOf(List<Card> cards, String name) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Card not found in list: " + name);
    }
}
