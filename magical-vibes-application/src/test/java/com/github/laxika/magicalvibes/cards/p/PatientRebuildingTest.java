package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatientRebuildingTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger only offers opponents as targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new PatientRebuilding());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Opponent mills three and the controller draws one card per land milled")
    void millsThreeAndDrawsPerLand() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        setDeck(player2, new Mountain(), new GrizzlyBears(), new Mountain(), new GrizzlyBears());

        advanceToUpkeep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    @DisplayName("No draws when no land cards are milled")
    void noDrawsWithoutLands() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        setDeck(player2, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());

        advanceToUpkeep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library is smaller than three")
    void millsOnlyRemainingWhenLibrarySmall() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        setDeck(player2, new Mountain(), new GrizzlyBears());

        advanceToUpkeep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    private void setDeck(Player player, Card... cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
