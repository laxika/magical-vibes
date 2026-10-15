package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KodamasReach;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CranialExtraction.class, SakuraTribeElder.class, KodamasReach.class, Island.class})
class CranialExtractionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts the caster for a card name")
    void resolvingPromptsForCardName() {
        harness.setHand(player1, List.of(new CranialExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Exiles matching cards from the target player's hand, graveyard and library")
    void exilesMatchingCardsFromAllZones() {
        Card bears1 = new SakuraTribeElder();
        Card bears2 = new SakuraTribeElder();
        Card bears3 = new SakuraTribeElder();
        Card peek = new KodamasReach();

        harness.setHand(player2, new ArrayList<>(List.of(bears1, peek)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));
        harness.setLibrary(player2, List.of(bears3));

        harness.setHand(player1, List.of(new CranialExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Sakura-Tribe Elder");
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId(), bears3.getId()));

        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Sakura-Tribe Elder"))
                .count();
        assertThat(exiledCount).isEqualTo(3);
        harness.assertNotInHand(player2, "Sakura-Tribe Elder");
        harness.assertNotInGraveyard(player2, "Sakura-Tribe Elder");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Sakura-Tribe Elder"));
        harness.assertInHand(player2, "Kodama's Reach");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("May exile fewer than all matching cards")
    void allowsPartialSelection() {
        Card bears1 = new SakuraTribeElder();
        Card bears2 = new SakuraTribeElder();
        harness.setHand(player2, new ArrayList<>(List.of(bears1, bears2)));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of());

        harness.setHand(player1, List.of(new CranialExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Sakura-Tribe Elder");

        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears2);
    }

    @Test
    @DisplayName("Naming a card with no copies exiles nothing and leaves the library intact")
    void noMatchesExilesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new KodamasReach())));
        harness.setGraveyard(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new CranialExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Sakura-Tribe Elder");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cranial Extraction");
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetSelf() {
        Card bears = new SakuraTribeElder();
        harness.setHand(player1, new ArrayList<>(List.of(new CranialExtraction(), bears)));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.handleListChoice(player1, "Sakura-Tribe Elder");
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sakura-Tribe Elder"));
        harness.assertNotInHand(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("All matching graveyard cards are exiled even when hidden-zone copies are declined")
    void mustExileEveryMatchingGraveyardCard() {
        Card inHand = new SakuraTribeElder();
        Card inLibrary = new SakuraTribeElder();
        Card inGraveyard1 = new SakuraTribeElder();
        Card inGraveyard2 = new SakuraTribeElder();
        harness.setHand(player2, List.of(inHand));
        harness.setLibrary(player2, List.of(inLibrary));
        harness.setGraveyard(player2, List.of(inGraveyard1, inGraveyard2));
        harness.setHand(player1, List.of(new CranialExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Sakura-Tribe Elder");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(inGraveyard1, inGraveyard2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(inLibrary);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May decline all matching cards in hidden zones and still shuffle")
    void mayDeclineAllHiddenZoneCopies() {
        Card inHand = new SakuraTribeElder();
        Card inLibrary = new SakuraTribeElder();
        harness.setHand(player2, List.of(inHand));
        harness.setLibrary(player2, List.of(inLibrary));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new CranialExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Sakura-Tribe Elder");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(inLibrary);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("shuffles their library"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The resolving spell offers nonland names but excludes land names")
    void cannotNameALand() {
        harness.setHand(player2, List.of(new Island(), new SakuraTribeElder()));
        harness.setHand(player1, List.of(new CranialExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Sakura-Tribe Elder").doesNotContain("Island");
    }
}
