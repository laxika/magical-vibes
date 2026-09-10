package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BringToLight.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class})
class BringToLightTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for eligible cards with mana value at most the Converge count")
    void searchesWithinConvergeManaValue() {
        castWithMana(List.of(new Forest(), new GrizzlyBears(), new Shock(), new Divination()),
                3, ManaColor.GREEN, ManaColor.BLUE);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);

        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Shock");
    }

    @Test
    @DisplayName("Three colors of mana increase the search limit to three")
    void countsThreeDistinctColors() {
        castWithMana(List.of(new GrizzlyBears(), new Divination()),
                0, ManaColor.GREEN, ManaColor.BLUE,
                ManaColor.BLACK, ManaColor.BLACK, ManaColor.BLACK);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);

        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Divination");
    }

    @Test
    @DisplayName("Exiles the chosen card and offers it for a free cast")
    void exilesAndCastsChosenCardWithoutPaying() {
        castWithMana(List.of(new GrizzlyBears()), 3, ManaColor.GREEN, ManaColor.BLUE);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castWithMana(List<Card> library, int colorlessMana, ManaColor... colors) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new BringToLight()));
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        for (ManaColor color : colors) {
            harness.addMana(player1, color, 1);
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
