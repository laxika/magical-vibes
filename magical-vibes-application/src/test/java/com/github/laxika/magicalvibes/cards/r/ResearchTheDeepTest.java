package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResearchTheDeepTest extends BaseCardTest {

    private void castResearch() {
        harness.setHand(player1, List.of(new ResearchTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws the top card of the controller's library")
    void drawsACard() {
        // Draw happens first (Island), then the clash reveals the next card (Forest, MV 0) → equal → loss.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player1.getId()).addFirst(new Island());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        castResearch();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Winning the clash returns Research the Deep to its owner's hand")
    void wonClashReturnsSpellToHand() {
        // Draw Forest, then clash with Grizzly Bears (MV 2) vs opponent's Forest (MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        castResearch();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Research the Deep"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Research the Deep"));
    }

    @Test
    @DisplayName("Losing the clash sends Research the Deep to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        // Draw Forest, then clash with Forest (MV 0) vs opponent's Grizzly Bears (MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        castResearch();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Research the Deep"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Research the Deep"));
    }
}
