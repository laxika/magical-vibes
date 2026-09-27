package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchaeomancersMap.class, Forest.class, GrizzlyBears.class, Plains.class, VolrathsStronghold.class})
class ArchaeomancersMapTest extends BaseCardTest {

    @Test
    @DisplayName("Entering searches for up to two basic Plains cards")
    void enteringSearchesForBasicPlains() {
        Plains first = new Plains();
        Plains second = new Plains();
        harness.setLibrary(player1, List.of(first, new Forest(), second, new GrizzlyBears()));
        castMap();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().cards()).containsExactly(first, second);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("When the entering opponent controls more lands, you may put a land from hand onto the battlefield")
    void putsLandFromHandAfterOpponentLandEnters() {
        harness.addToBattlefield(player1, new ArchaeomancersMap());
        harness.addToBattlefield(player2, new Forest());
        VolrathsStronghold land = new VolrathsStronghold();
        harness.setHand(player1, List.of(land));

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertNotInHand(player1, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("Does not offer the land drop when the entering opponent does not have more lands")
    void doesNotOfferLandDropWhenOpponentDoesNotHaveMoreLands() {
        harness.addToBattlefield(player1, new ArchaeomancersMap());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new VolrathsStronghold()));

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Volrath's Stronghold");
        harness.assertNotOnBattlefield(player1, "Volrath's Stronghold");
    }

    private void castMap() {
        harness.castFromHand(player1, new ArchaeomancersMap(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
