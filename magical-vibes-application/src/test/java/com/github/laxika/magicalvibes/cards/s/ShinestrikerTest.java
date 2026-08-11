package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShinestrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card when it is the only colored permanent you control")
    void drawsOneForOneColor() {
        castShinestriker();

        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws one card for each distinct color among permanents you control")
    void drawsForDistinctColors() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new AirElemental(), new HillGiant()));

        castShinestriker();

        assertThat(drawnCards(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only permanents controlled by its controller")
    void ignoresOpponentColors() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castShinestriker();

        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    private void castShinestriker() {
        harness.setHand(player1, List.of(new Shinestriker()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new AirElemental(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int drawnCards(Player player) {
        return gd.playerHands.get(player.getId()).size();
    }
}
