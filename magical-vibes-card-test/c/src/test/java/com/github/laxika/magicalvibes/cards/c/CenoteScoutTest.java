package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CenoteScout.class, Forest.class})
class CenoteScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Exploring a land puts it into its controller's hand")
    void exploringLandPutsItIntoHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        castCenoteScout();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(findCenoteScout().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Exploring a nonland puts a +1/+1 counter on Cenote Scout")
    void exploringNonlandPutsCounterOnScout() {
        Card nonland = new CenoteScout();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castCenoteScout();

        assertThat(findCenoteScout().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining the nonland graveyard choice leaves the card on top")
    void decliningNonlandGraveyardChoiceLeavesCardOnTop() {
        Card nonland = new CenoteScout();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castCenoteScout();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonland.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonland.getId()));
    }

    @Test
    @DisplayName("Accepting the nonland graveyard choice puts the card into the graveyard")
    void acceptingNonlandGraveyardChoicePutsCardIntoGraveyard() {
        Card nonland = new CenoteScout();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castCenoteScout();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nonland.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonland.getId()));
    }

    @Test
    @DisplayName("Exploring an empty library still puts a +1/+1 counter on Cenote Scout")
    void exploringEmptyLibraryPutsCounterOnScout() {
        harness.setLibrary(player1, List.of());

        castCenoteScout();

        assertThat(findCenoteScout().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exploring an empty library still counts as exploring this turn")
    void exploringEmptyLibraryRecordsExploreEvent() {
        harness.setLibrary(player1, List.of());

        castCenoteScout();

        assertThat(gd.playersWhoControlledPermanentThatExploredThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Exploring uses only the controller's library and hand")
    void exploringDoesNotUseOpponentsLibrary() {
        Card land = new Forest();
        Card opponentTopCard = new CenoteScout();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(opponentTopCard));
        List<Card> opponentHand = List.copyOf(gd.playerHands.get(player2.getId()));

        castCenoteScout();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHand);
    }

    private void castCenoteScout() {
        harness.castFromHand(player1, new CenoteScout(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findCenoteScout() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getClass() == CenoteScout.class)
                .findFirst()
                .orElseThrow();
    }
}
