package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EladamrisCall.class, AncientSpider.class, ForsakenCity.class})
class EladamrisCallTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a creature card and puts it into hand")
    void searchesForCreatureCard() {
        AncientSpider creature = new AncientSpider();
        ForsakenCity nonCreature = new ForsakenCity();
        harness.setLibrary(player1, List.of(creature, nonCreature));
        cast();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(creature);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Ancient Spider");
        harness.assertNotInHand(player1, "Forsaken City");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()))
                .contains(nonCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not offer noncreature cards")
    void doesNotOfferNoncreatures() {
        ForsakenCity nonCreature = new ForsakenCity();
        harness.setLibrary(player1, List.of(nonCreature));
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonCreature);
        harness.assertNotInHand(player1, "Forsaken City");
    }

    @Test
    @DisplayName("May fail to find a creature card")
    void mayFailToFindCreatureCard() {
        AncientSpider creature = new AncientSpider();
        ForsakenCity nonCreature = new ForsakenCity();
        harness.setLibrary(player1, List.of(creature, nonCreature));
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, nonCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast() {
        harness.castFromHand(player1, new EladamrisCall(), "{G}{W}");
    }
}
