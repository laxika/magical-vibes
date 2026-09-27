package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.o.OneWithNothing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeverendingTorment.class, ArabaMothrider.class, OneWithNothing.class})
class NeverendingTormentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles as many cards as are in your hand and applies Epic")
    void exilesCardsEqualToHandSizeAndAppliesEpic() {
        Card mothrider = new ArabaMothrider();
        Card oneWithNothing = new OneWithNothing();
        Card secondMothrider = new ArabaMothrider();
        Card secondOneWithNothing = new OneWithNothing();
        harness.setLibrary(player2, List.of(mothrider, oneWithNothing, secondMothrider, secondOneWithNothing));
        harness.setHand(player1, List.of(new NeverendingTorment(), new OneWithNothing(), new ArabaMothrider()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseLibraryCard(0);
        chooseLibraryCard(0);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(mothrider, oneWithNothing);
        assertThat(gd.findExiledCard(mothrider.getId()).faceDown()).isFalse();
        assertThat(gd.findExiledCard(oneWithNothing.getId()).faceDown()).isFalse();

        harness.setHand(player1, List.of(new OneWithNothing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Copies the search at the beginning of your upkeep")
    void copiesSearchAtUpkeep() {
        harness.setLibrary(player2, List.of(new ArabaMothrider(), new OneWithNothing()));
        harness.setHand(player1, List.of(new NeverendingTorment(), new OneWithNothing()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        harness.setLibrary(player2, List.of(new ArabaMothrider(), new OneWithNothing()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target your own library")
    void canTargetYourOwnLibrary() {
        Card libraryCard = new ArabaMothrider();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new NeverendingTorment(), new OneWithNothing()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Applies Epic when your hand is empty and exiles no cards")
    void appliesEpicWithEmptyHand() {
        Card mothrider = new ArabaMothrider();
        Card oneWithNothing = new OneWithNothing();
        harness.setLibrary(player2, List.of(mothrider, oneWithNothing));
        harness.setHand(player1, List.of(new NeverendingTorment()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(mothrider, oneWithNothing);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playersCantCastSpellsForRestOfGame).contains(player1.getId());
    }

    @Test
    @DisplayName("Can choose a new target for the Epic copy")
    void canRetargetEpicCopy() {
        Card originalTargetCard = new ArabaMothrider();
        Card newTargetCard = new OneWithNothing();
        harness.setLibrary(player2, List.of(originalTargetCard));
        harness.setHand(player1, List.of(new NeverendingTorment(), new OneWithNothing()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        harness.setLibrary(player1, List.of(newTargetCard));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(originalTargetCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(newTargetCard);
    }

    private void chooseLibraryCard(int index) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, index);
    }
}
