package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YanlingsHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Yanling's Harbinger triggers a may ability")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability returns Mu Yanling, Celestial Wind from the graveyard")
    void acceptingMayFindsMuYanlingInGraveyard() {
        harness.setGraveyard(player1, List.of(createMuYanling()));
        setupAndCast();

        resolveMay(true);

        harness.assertInHand(player1, "Mu Yanling, Celestial Wind");
        harness.assertNotInGraveyard(player1, "Mu Yanling, Celestial Wind");
    }

    @Test
    @DisplayName("Accepting the may ability searches the library when Mu Yanling is not in the graveyard")
    void acceptingMaySearchesLibrary() {
        Card muYanling = createMuYanling();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(muYanling);
        setupAndCast();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName()).isEqualTo("Mu Yanling, Celestial Wind");
    }

    @Test
    @DisplayName("Declining the may ability leaves Mu Yanling in the graveyard")
    void decliningMayDoesNotSearch() {
        harness.setGraveyard(player1, List.of(createMuYanling()));
        setupAndCast();

        resolveMay(false);

        harness.assertInGraveyard(player1, "Mu Yanling, Celestial Wind");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new YanlingsHarbinger()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
    }

    private void resolveMay(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private Card createMuYanling() {
        Card muYanling = new Card();
        muYanling.setName("Mu Yanling, Celestial Wind");
        muYanling.setType(CardType.PLANESWALKER);
        muYanling.setManaCost("{3}{U}{U}");
        return muYanling;
    }
}
