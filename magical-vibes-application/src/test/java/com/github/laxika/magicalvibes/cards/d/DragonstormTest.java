package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dragonstorm.class, DragonTyrant.class, ScornfulEgotist.class})
class DragonstormTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only Dragon permanent cards")
    void resolvingPresentsOnlyDragons() {
        castDragonstorm();
        setupLibrary();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Dragon Tyrant");
        assertThat(offered).allMatch(c -> c.getSubtypes().contains(CardSubtype.DRAGON));
    }

    @Test
    @DisplayName("Choosing a Dragon puts it onto the battlefield")
    void choosingDragonPutsItOntoBattlefield() {
        castDragonstorm();
        setupLibrary();

        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Dragon Tyrant");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Scornful Egotist");
    }

    @Test
    @DisplayName("The search may fail to find a Dragon")
    void searchMayFailToFindDragon() {
        Card nonDragon = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(nonDragon));
        castDragonstorm();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Dragon Tyrant");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Storm copies the spell once for each spell cast before it this turn")
    void stormCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new ScornfulEgotist());
        gd.recordSpellCast(player2.getId(), new ScornfulEgotist());

        castDragonstorm();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy))
                .allMatch(e -> e.getCard().getName().equals("Dragonstorm"));
    }

    @Test
    @DisplayName("Cast as the first spell of the turn, Storm creates no copies")
    void stormWithNoPriorSpellsCreatesNoCopies() {
        castDragonstorm();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Dragonstorm"));
    }

    @Test
    @DisplayName("Each Storm copy resolves Dragonstorm's library search")
    void stormCopiesResolveLibrarySearch() {
        Card firstDragon = new DragonTyrant();
        Card secondDragon = new DragonTyrant();
        harness.setLibrary(player1, List.of(firstDragon, secondDragon));
        gd.recordSpellCast(player1.getId(), new ScornfulEgotist());

        castDragonstorm();
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Dragon Tyrant", "Dragon Tyrant");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castDragonstorm() {
        harness.castFromHand(player1, new Dragonstorm(), "{8}{R}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new DragonTyrant(), new ScornfulEgotist()));
    }
}
