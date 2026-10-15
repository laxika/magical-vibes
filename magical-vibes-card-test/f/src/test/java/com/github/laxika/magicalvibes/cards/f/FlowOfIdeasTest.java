package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ConsignToDream;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WateryGrave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowOfIdeas.class, Island.class, Swamp.class, WateryGrave.class, ConsignToDream.class})
class FlowOfIdeasTest extends BaseCardTest {

    private void castFlowOfIdeas() {
        harness.castFromHand(player1, new FlowOfIdeas(), "{5}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws one card per Island the caster controls")
    void drawsPerIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castFlowOfIdeas();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Only the caster's Islands are counted, not the opponent's")
    void ignoresOpponentsIslands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castFlowOfIdeas();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Non-Island lands are not counted")
    void ignoresNonIslandLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castFlowOfIdeas();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Counts a nonbasic land with the Island subtype")
    void countsNonbasicIsland() {
        harness.addToBattlefield(player1, new WateryGrave());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castFlowOfIdeas();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Counts Islands when the spell resolves")
    void countsIslandsAtResolution() {
        harness.castFromHand(player1, new FlowOfIdeas(), "{5}{U}");
        harness.addToBattlefield(player1, new Island());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws no cards when the caster controls no Islands")
    void drawsNoCardsWithoutIslands() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castFlowOfIdeas();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("An Island returned to hand before resolution is not counted")
    void excludesIslandThatLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new Island());
        var island = gd.playerBattlefields.get(player1.getId()).getFirst();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFromHand(player1, new FlowOfIdeas(), "{5}{U}");
        harness.setHand(player2, List.of(new ConsignToDream()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player2, 0, island.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Drawn cards enter only the caster's hand in library order")
    void putsDrawnCardsIntoCastersHand() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        var first = new Swamp();
        var second = new Island();
        var remaining = new Swamp();
        harness.setLibrary(player1, List.of(first, second, remaining));
        var opponentHandBefore = List.copyOf(gd.playerHands.get(player2.getId()));
        var opponentLibraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));

        castFlowOfIdeas();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibraryBefore);
    }
}
