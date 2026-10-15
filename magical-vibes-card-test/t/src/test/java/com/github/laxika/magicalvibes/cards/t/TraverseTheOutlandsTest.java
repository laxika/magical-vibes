package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PastInFlames;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TraverseTheOutlands.class, Forest.class, Island.class, Mountain.class,
        GrizzlyBears.class, HillGiant.class, PastInFlames.class})
class TraverseTheOutlandsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to the greatest power in basic lands and puts them onto the battlefield tapped")
    void searchesForBasicLandsEqualToGreatestPower() {
        harness.addToBattlefield(player1, new HillGiant());
        List<Card> library = List.of(new Forest(), new Island(), new Mountain(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        castTraverseTheOutlands();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().remainingCount()).isEqualTo(3);

        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Island").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Mountain").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Can find fewer than the greatest power")
    void canFindFewerThanTheGreatestPower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castTraverseTheOutlands();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanents(player1, "Forest").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Island")).isEmpty();
    }

    @Test
    @DisplayName("Chosen lands enter together after all search choices are complete")
    void chosenLandsEnterTogether() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        castTraverseTheOutlands();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanents(player1, "Island")).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without controlled creatures no lands are found, even with an opposing creature")
    void noControlledCreaturesFindsNoLandsAndShuffles() {
        harness.addToBattlefield(player2, new HillGiant());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castTraverseTheOutlands();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    @DisplayName("Uses the greatest individual power rather than the sum or an opponent's power")
    void usesGreatestControlledPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        castTraverseTheOutlands();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanents(player1, "Island")).hasSize(1);
        assertThat(findPermanents(player1, "Mountain")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Determines greatest power on resolution after a creature leaves the battlefield")
    void determinesPowerOnResolution() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        castTraverseTheOutlands();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Hill Giant"));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanents(player1, "Island")).hasSize(1);
        assertThat(findPermanents(player1, "Mountain")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can choose zero lands even when matching basic lands exist")
    void canChooseZeroLands() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castTraverseTheOutlands();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(findPermanents(player1, "Island")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    @DisplayName("Granted flashback still searches according to greatest controlled power")
    void grantedFlashbackUsesGreatestPower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setGraveyard(player1, List.of(new TraverseTheOutlands()));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().remainingCount()).isEqualTo(3);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Traverse the Outlands"));
    }

    private void castTraverseTheOutlands() {
        harness.setHand(player1, List.of(new TraverseTheOutlands()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
