package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.cards.u.UrborgRepossession;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicEpiphany.class, UrborgRepossession.class, LightningStrike.class, NishobaBrawler.class})
class CosmicEpiphanyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each instant or sorcery in controller's graveyard")
    void drawsForEachInstantOrSorceryInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new LightningStrike(), new UrborgRepossession(), new NishobaBrawler()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castCosmicEpiphany();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Does not count instant or sorcery cards in an opponent's graveyard")
    void onlyCountsControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new LightningStrike()));
        harness.setGraveyard(player2, List.of(new LightningStrike(), new UrborgRepossession()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castCosmicEpiphany();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws no cards when controller has no instant or sorcery cards in their graveyard")
    void drawsNoCardsWithoutInstantOrSorceryCards() {
        harness.setGraveyard(player1, List.of(new NishobaBrawler()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castCosmicEpiphany();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Counts another Cosmic Epiphany in the graveyard but not the resolving spell")
    void countsOtherCopyInGraveyard() {
        harness.setGraveyard(player1, List.of(new CosmicEpiphany()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castCosmicEpiphany();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Counts an instant that enters the graveyard after Cosmic Epiphany is cast")
    void countsCardsAtResolution() {
        harness.setGraveyard(player1, List.of(new UrborgRepossession()));
        harness.setHand(player1, List.of(new CosmicEpiphany(), new LightningStrike()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.addMana(player1, ManaColor.RED, 1);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Lightning Strike");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        harness.assertInGraveyard(player1, "Cosmic Epiphany");
    }

    private void castCosmicEpiphany() {
        harness.setHand(player1, List.of(new CosmicEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
