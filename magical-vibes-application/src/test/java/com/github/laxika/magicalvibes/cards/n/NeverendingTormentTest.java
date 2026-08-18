package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeverendingTormentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles as many cards as are in your hand and applies Epic")
    void exilesCardsEqualToHandSizeAndAppliesEpic() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card swamp = new Swamp();
        Card secondBears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears, shock, swamp, secondBears));
        harness.setHand(player1, List.of(new NeverendingTorment(), new Shock(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseLibraryCard(0);
        chooseLibraryCard(0);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears, shock);
        assertThat(gd.findExiledCard(bears.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(shock.getId()).faceDown()).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Copies the search at the beginning of your upkeep")
    void copiesSearchAtUpkeep() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Swamp()));
        harness.setHand(player1, List.of(new NeverendingTorment(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        harness.setLibrary(player2, List.of(new Shock(), new Swamp()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    private void chooseLibraryCard(int index) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
