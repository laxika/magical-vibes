package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanarEngineeringTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices two lands before offering basic lands from the library")
    void sacrificesTwoLandsBeforeSearching() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        setLibrary(new Forest(), new Island(), new GrizzlyBears());

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
        setLibrary(new Forest(), new Island(), new Mountain(), new Plains(), new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(4)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        for (int i = 0; i < 4; i++) {
            harness.getGameService().handleInteractionAnswer(
                    gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isTapped)
                .extracting(Permanent::getCard)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("Can fail to find and still sacrifices the lands")
    void canFailToFindAfterSacrificing() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new PlanarEngineering()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        setLibrary(new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
