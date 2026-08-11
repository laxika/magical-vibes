package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoggleRobberTest extends BaseCardTest {

    @Test
    @DisplayName("When Noggle Robber enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.setHand(player1, List.of(new NoggleRobber()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("When Noggle Robber dies, it creates a Treasure token")
    void deathCreatesTreasureToken() {
        harness.addToBattlefield(player1, new NoggleRobber());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
    }
}
