package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarEngineering.class, Mountain.class, Island.class, Forest.class, Plains.class})
class PlanarEngineeringTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices two lands before offering basic lands from the library")
    void sacrificesTwoLandsBeforeSearching() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new PlanarEngineering()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Chosen basic lands enter the battlefield tapped")
    void chosenBasicLandsEnterTapped() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains(), new PlanarEngineering()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(4)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isTapped)
                .extracting(Permanent::getCard)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(PlanarEngineering.class);
    }

    @Test
    @DisplayName("Can fail to find and still sacrifices the lands")
    void canFailToFindAfterSacrificing() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new PlanarEngineering()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gameLogContains("finds no basic land cards")).isTrue();
    }

    @Test
    @DisplayName("Searches even when there are no lands to sacrifice")
    void searchesWithoutLands() {
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifices the only land and still searches for four")
    void searchesAfterSacrificingOnlyLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Mountain");
        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(countPermanents(player1, "Forest")).isEqualTo(4);
        assertThat(findPermanents(player1, "Forest")).allMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can decline to find even when basic lands are available")
    void declinesAvailableLands() {
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chooses exactly two lands when more than two are controlled")
    void choosesTwoLandsToSacrifice() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new Forest()));
        var mountainId = findPermanent(player1, "Mountain").getId();
        var islandId = findPermanent(player1, "Island").getId();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        harness.handleMultiplePermanentsChosen(player1, List.of(mountainId, islandId));
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(countPermanents(player1, "Plains")).isEqualTo(1);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Selected lands wait until selection is complete before entering together")
    void landsEnterTogetherAfterSelection() {
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4).allMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
