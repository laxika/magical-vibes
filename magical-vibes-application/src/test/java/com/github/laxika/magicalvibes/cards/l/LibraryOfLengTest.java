package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BurningInquiry;
import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LibraryOfLeng.class, BurningInquiry.class, Catalog.class, GrizzlyBears.class, Island.class})
class LibraryOfLengTest extends BaseCardTest {

    @Test
    @DisplayName("A chosen discard by the controller goes on top of their library instead of the graveyard")
    void chosenDiscardGoesOnTopOfLibrary() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Catalog drew two Islands; hand is GrizzlyBears + 2 Islands. Discard the GrizzlyBears.
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // The discarded Grizzly Bears is put on top of the library, not into the graveyard.
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Catalog");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void controllerMayDeclineLibraryReplacement() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Random discards by the controller go on top of library; opponent without Leng still discards to graveyard")
    void randomDiscardRedirectOnlyForController() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player 1 drew 3 and discarded 3 at random; with Library of Leng they go back on top of
        // the library, so the deck size is unchanged and only Burning Inquiry hits the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement()
                .matches(c -> c.getName().equals("Burning Inquiry"));

        // Player 2 has no Library of Leng — their 3 random discards go to the graveyard as normal.
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
