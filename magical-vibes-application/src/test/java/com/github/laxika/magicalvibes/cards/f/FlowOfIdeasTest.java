package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WateryGrave;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowOfIdeas.class, Island.class, Swamp.class, WateryGrave.class})
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
}
