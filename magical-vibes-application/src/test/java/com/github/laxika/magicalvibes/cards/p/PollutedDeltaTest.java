package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PollutedDeltaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Polluted Delta pays 1 life, sacrifices it, and searches for an Island or Swamp")
    void activationPaysLifeSacrificesAndSearchesForMatchingLand() {
        activateSearch();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Polluted Delta");
        harness.assertInGraveyard(player1, "Polluted Delta");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Island") || card.getName().equals("Swamp"))
                .noneMatch(card -> card.getName().equals("Mountain") || card.getName().equals("Grizzly Bears"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Island or Swamp enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> (permanent.getCard().getName().equals("Island")
                        || permanent.getCard().getName().equals("Swamp")) && !permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new PollutedDelta());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Island(), new Swamp(), new Mountain(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
    }
}
