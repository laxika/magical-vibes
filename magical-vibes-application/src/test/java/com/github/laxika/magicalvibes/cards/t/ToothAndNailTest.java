package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToothAndNailTest extends BaseCardTest {

    @Test
    @DisplayName("Search mode puts up to two creature cards into your hand")
    void searchesForCreatures() {
        cast(new int[]{0}, false, List.of(new ToothAndNail()),
                List.of(new GrizzlyBears(), new LlanowarElves(), new Forest()));

        chooseLibraryCard();
        chooseLibraryCard();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Battlefield mode puts up to two creature cards from hand onto the battlefield")
    void putsCreaturesFromHand() {
        cast(new int[]{1}, false,
                List.of(new ToothAndNail(), new GrizzlyBears(), new LlanowarElves()), List.of());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Entwine resolves both modes")
    void entwinesBothModes() {
        cast(new int[]{0, 1}, true, List.of(new ToothAndNail()),
                List.of(new GrizzlyBears(), new LlanowarElves()));

        chooseLibraryCard();
        chooseLibraryCard();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Entwine requires its additional two generic mana")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new ToothAndNail()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, boolean entwined, List<Card> hand, List<Card> library) {
        harness.setHand(player1, hand);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 7 : 5);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }

    private void chooseLibraryCard() {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }
}
