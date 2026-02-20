package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommuneWithNatureTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Commune with Nature has correct card properties")
    void hasCorrectProperties() {
        CommuneWithNature card = new CommuneWithNature();

        assertThat(card.getName()).isEqualTo("Commune with Nature");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect.class);
        LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect effect =
                (LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(5);
        assertThat(effect.cardTypes()).containsExactly(CardType.CREATURE);
    }

    @Test
    @DisplayName("Casting Commune with Nature puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Commune with Nature");
    }

    @Test
    @DisplayName("Resolves by offering only creature cards among top five")
    void resolvesOfferingOnlyCreatures() {
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingLibrarySearchCanFailToFind()).isTrue();
        assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(2);
        assertThat(gd.interaction.awaitingLibrarySearchCards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand then orders rest on bottom")
    void choosingCreatureThenOrderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        setupTopFive(List.of(elves, shock, bears, plains, swamp));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose Llanowar Elves
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(4);

        List<Card> remaining = gd.interaction.awaitingLibraryReorderCards();
        // Put Plains first (closest to top), then Shock, then Swamp, then Grizzly Bears
        int iPlains = indexOf(remaining, "Plains");
        int iShock = indexOf(remaining, "Shock");
        int iSwamp = indexOf(remaining, "Swamp");
        int iBears = indexOf(remaining, "Grizzly Bears");
        harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(iPlains, iShock, iSwamp, iBears));

        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Plains", "Shock", "Swamp", "Grizzly Bears");
    }

    @Test
    @DisplayName("You may choose no creature card and still reorder all looked cards to bottom")
    void mayChooseNoCreature() {
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(5);
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
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.awaitingLibraryReorderCards()).hasSize(5);
    }

    @Test
    @DisplayName("With empty library, Commune with Nature does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Commune with Nature goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new CommuneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Commune with Nature"));
        assertThat(gd.stack).isEmpty();
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
