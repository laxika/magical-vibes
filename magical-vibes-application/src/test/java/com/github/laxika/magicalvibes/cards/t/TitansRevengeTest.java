package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TitansRevengeTest extends BaseCardTest {

    private void castAtPlayer2(int x) {
        harness.setHand(player1, List.of(new TitansRevenge()));
        harness.addMana(player1, ManaColor.RED, 2 + x);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, x, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals X damage to any target")
    void dealsXDamageToTarget() {
        // Equal mana values on top → clash is a loss, isolating the damage effect.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        castAtPlayer2(4);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Winning the clash returns Titan's Revenge to its owner's hand")
    void wonClashReturnsSpellToHand() {
        // Higher mana value on top for player1 (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        castAtPlayer2(3);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Titan's Revenge"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Titan's Revenge"));
    }

    @Test
    @DisplayName("Losing the clash sends Titan's Revenge to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        // Lower mana value on top for player1 (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        castAtPlayer2(3);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Titan's Revenge"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Titan's Revenge"));
    }
}
