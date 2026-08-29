package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianasStandardBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each creature that died under its controller's control this turn")
    void drawsForControllerCreatureDeaths() {
        gd.creatureDeathCountThisTurn.put(player1.getId(), 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LilianasStandardBearer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 2);
    }

    @Test
    @DisplayName("Does not draw for creatures that died under an opponent's control")
    void onlyCountsControllerCreatureDeaths() {
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, List.of(new LilianasStandardBearer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }
}
