package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlowOfIdeasTest extends BaseCardTest {

    private void castFlowOfIdeas() {
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws one card per Island the caster controls")
    void drawsPerIsland() {
        harness.setHand(player1, new ArrayList<>(List.of(new FlowOfIdeas())));
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
        harness.setHand(player1, new ArrayList<>(List.of(new FlowOfIdeas())));
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
        harness.setHand(player1, new ArrayList<>(List.of(new FlowOfIdeas())));
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castFlowOfIdeas();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }
}
