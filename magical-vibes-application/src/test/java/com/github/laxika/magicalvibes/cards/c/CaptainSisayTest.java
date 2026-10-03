package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainSisay.class, CrosisThePurger.class, AncientKavu.class})
class CaptainSisayTest extends BaseCardTest {

    private void setUpCaptainSisay() {
        addCreatureReady(player1, new CaptainSisay());
        harness.setLibrary(player1, List.of());
    }

    @Test
    @DisplayName("The tap ability offers only legendary cards and puts the chosen card into hand")
    void searchesForLegendaryCard() {
        setUpCaptainSisay();
        CrosisThePurger crosis = new CrosisThePurger();
        harness.setLibrary(player1, List.of(crosis, new AncientKavu()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(findPermanent(player1, "Captain Sisay").isTapped()).isTrue();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Crosis, the Purger");
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Crosis, the Purger");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search does not offer nonlegendary cards")
    void doesNotOfferNonlegendaryCards() {
        setUpCaptainSisay();
        harness.setLibrary(player1, List.of(new AncientKavu()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Ancient Kavu");
    }

    @Test
    @DisplayName("The controller may fail to find even when a legendary card is available")
    void mayFailToFindLegendaryCard() {
        setUpCaptainSisay();
        CrosisThePurger crosis = new CrosisThePurger();
        AncientKavu kavu = new AncientKavu();
        harness.setLibrary(player1, List.of(crosis, kavu));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Crosis, the Purger");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(crosis, kavu);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Searching an empty library completes without a choice")
    void emptyLibrarySearchCompletes() {
        setUpCaptainSisay();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Captain Sisay").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
