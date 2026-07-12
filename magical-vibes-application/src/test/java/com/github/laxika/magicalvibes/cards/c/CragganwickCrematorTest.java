package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CragganwickCrematorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB: discarding a creature card deals its power to target player")
    void discardsCreatureDealsPowerToPlayer() {
        // Only one card left in hand after casting, so the random discard is deterministic.
        harness.setHand(player1, List.of(new CragganwickCremator(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(player2.getId()), List.of());

        // Resolve creature spell → enters battlefield, ETB triggers.
        harness.passBothPriorities();
        // Resolve ETB triggered ability → discard Grizzly Bears (2/2), deal 2 to player2.
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB: discarding a noncreature card deals no damage")
    void discardsNoncreatureDealsNoDamage() {
        harness.setHand(player1, List.of(new CragganwickCremator(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(player2.getId()), List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Mountain is discarded (non-creature) — the ability has no further effect.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB: still discards a card even with no legal creature to fuel damage")
    void discardsEvenWhenNoDamage() {
        harness.setHand(player1, List.of(new CragganwickCremator(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(player2.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Hand is emptied by the random discard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
