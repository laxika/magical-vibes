package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObsessiveSearch.class, AngelOfRetribution.class, Unhinge.class})
class ObsessiveSearchTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card")
    void drawsACard() {
        harness.setLibrary(player1, List.of(new AngelOfRetribution()));
        harness.castFromHand(player1, new ObsessiveSearch(), "{U}");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Angel of Retribution");
        harness.assertInGraveyard(player1, "Obsessive Search");
    }

    @Test
    @DisplayName("Accepting madness draws a card")
    void acceptingMadnessDrawsACard() {
        discardViaUnhinge();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Angel of Retribution");
        harness.assertInGraveyard(player1, "Obsessive Search");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining madness puts Obsessive Search into the graveyard")
    void decliningMadnessPutsCardIntoGraveyard() {
        ObsessiveSearch search = discardViaUnhinge();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(search.getId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Obsessive Search");
        harness.assertNotInHand(player1, "Angel of Retribution");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(search.getId()));
    }

    private ObsessiveSearch discardViaUnhinge() {
        ObsessiveSearch search = new ObsessiveSearch();
        harness.setHand(player1, List.of(search));
        harness.setLibrary(player1, List.of(new AngelOfRetribution()));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.setLibrary(player2, List.of(new AngelOfRetribution()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return search;
    }
}
