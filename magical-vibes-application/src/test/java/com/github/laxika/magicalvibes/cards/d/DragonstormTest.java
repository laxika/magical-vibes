package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonstormTest extends BaseCardTest {

    // ===== Library search =====

    @Test
    @DisplayName("Resolving presents only Dragon permanent cards")
    void resolvingPresentsOnlyDragons() {
        castDragonstorm();
        setupLibrary();

        harness.passBothPriorities(); // Storm trigger resolves (no prior spells → no copies)
        harness.passBothPriorities(); // Dragonstorm's search resolves

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).allMatch(c -> c.getSubtypes().contains(CardSubtype.DRAGON));
        assertThat(offered.stream().map(Card::getName)).contains("Dragon Whelp").doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing a Dragon puts it onto the battlefield")
    void choosingDragonPutsItOntoBattlefield() {
        castDragonstorm();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dragon Whelp"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Dragon Whelp"));
    }

    // ===== Storm =====

    @Test
    @DisplayName("Storm copies the spell once for each spell cast before it this turn")
    void stormCopiesForEachPriorSpell() {
        GameData gd = harness.getGameData();
        // Two spells already cast this turn (by any player).
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        castDragonstorm();

        harness.passBothPriorities(); // Storm trigger resolves — creates two copies

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy))
                .allMatch(e -> e.getCard().getName().equals("Dragonstorm"));
    }

    @Test
    @DisplayName("Cast as the first spell of the turn, Storm creates no copies")
    void stormWithNoPriorSpellsCreatesNoCopies() {
        castDragonstorm();

        harness.passBothPriorities(); // Storm trigger resolves — no copies

        GameData gd = harness.getGameData();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Dragonstorm"));
    }

    // ===== Helpers =====

    private void castDragonstorm() {
        harness.setHand(player1, List.of(new Dragonstorm()));
        harness.addMana(player1, ManaColor.RED, 9); // {8}{R}
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new DragonWhelp(), new GrizzlyBears()));
    }
}
