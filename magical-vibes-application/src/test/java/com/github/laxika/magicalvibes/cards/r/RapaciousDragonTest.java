package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RapaciousDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When Rapacious Dragon enters, two Treasure tokens are created")
    void etbCreatesTwoTreasureTokens() {
        harness.setHand(player1, List.of(new RapaciousDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(2);
        harness.assertOnBattlefield(player1, "Rapacious Dragon");
    }
}
