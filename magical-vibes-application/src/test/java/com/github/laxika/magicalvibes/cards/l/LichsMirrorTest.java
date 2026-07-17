package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LichsMirrorTest extends BaseCardTest {

    private static List<Card> shocks(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }

    @Test
    @DisplayName("Would-lose from lethal damage is replaced by the reset instead of ending the game")
    void resetsInsteadOfLosingFromLethalDamage() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setHand(player1, shocks(2));
        harness.setGraveyard(player1, shocks(1));
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        // The game continues and the player is reset rather than losing.
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerLifeTotals.get(p1)).isEqualTo(20);
        // Hand, graveyard, and all owned permanents were shuffled into the library, then 7 drawn.
        assertThat(gd.playerHands.get(p1)).hasSize(7);
        assertThat(gd.playerGraveyards.get(p1)).isEmpty();
        assertThat(gd.playerBattlefields.get(p1)).isEmpty();
        // 10 library + 2 hand + 1 graveyard + 1 Lich's Mirror = 14, minus 7 drawn = 7 left.
        assertThat(gd.playerDecks.get(p1)).hasSize(7);
    }

    @Test
    @DisplayName("Without Lich's Mirror the player loses normally at 0 life")
    void losesNormallyWithoutMirror() {
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Lich's Mirror shuffles itself away and can't save the same player twice")
    void doesNotSaveTwice() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        // The Mirror is a permanent the player owns, so it was shuffled into the library.
        assertThat(gd.playerBattlefields.get(p1)).isEmpty();

        // A second lethal state now finishes the game — the Mirror is gone.
        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
