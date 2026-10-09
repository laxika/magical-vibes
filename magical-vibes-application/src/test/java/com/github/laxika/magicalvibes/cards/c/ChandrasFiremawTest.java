package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandrasFiremaw.class, ChandraFlamesCatalyst.class})
class ChandrasFiremawTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Chandra's Firemaw offers its optional search")
    void resolvingOffersOptionalSearch() {
        castFiremaw();

        resolveCreature();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the search returns Chandra, Flame's Catalyst from the graveyard")
    void searchesGraveyard() {
        harness.setGraveyard(player1, List.of(new ChandraFlamesCatalyst()));
        castFiremaw();

        resolveMay(true);

        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));

        harness.assertInHand(player1, "Chandra, Flame's Catalyst");
        harness.assertNotInGraveyard(player1, "Chandra, Flame's Catalyst");
    }

    @Test
    @DisplayName("Accepting the search offers Chandra, Flame's Catalyst from the library")
    void searchesLibrary() {
        Card chandra = new ChandraFlamesCatalyst();
        harness.setLibrary(player1, List.of(chandra));
        castFiremaw();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chandra.getId()));

        harness.assertInHand(player1, "Chandra, Flame's Catalyst");
    }

    @Test
    @DisplayName("Declining the search leaves Chandra, Flame's Catalyst in the graveyard")
    void declinesSearch() {
        harness.setGraveyard(player1, List.of(new ChandraFlamesCatalyst()));
        castFiremaw();

        resolveMay(false);

        harness.assertInGraveyard(player1, "Chandra, Flame's Catalyst");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castFiremaw() {
        harness.setHand(player1, List.of(new ChandrasFiremaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveCreature() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveMay(boolean choice) {
        resolveCreature();
        harness.handleMayAbilityChosen(player1, choice);
    }
}
