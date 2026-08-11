package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiligentFarmhandTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and searches for a basic land onto the battlefield tapped")
    void sacrificesAndOffersBasicLands() {
        activateAbility();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Diligent Farmhand");
        harness.assertInGraveyard(player1, "Diligent Farmhand");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Forest") || card.getName().equals("Island"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("The searched basic land enters tapped")
    void searchedLandEntersTapped() {
        activateAbility();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest")
                        && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The basic land search may fail to find")
    void searchMayFailToFind() {
        harness.addToBattlefield(player1, new DiligentFarmhand());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest")
                        || permanent.getCard().getName().equals("Island"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateAbility() {
        harness.addToBattlefield(player1, new DiligentFarmhand());
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Island(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
    }
}
