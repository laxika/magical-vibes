package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfLostThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills four cards from target player's library")
    void etbMillsFourCardsFromTargetPlayersLibrary() {
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new Mountain(), new Plains()));
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("ETB can target its controller")
    void etbCanTargetItsController() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains()));
        harness.setHand(player1, List.of(new WallOfLostThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }
}
