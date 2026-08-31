package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({TivadarsCrusade.class, GoblinHero.class, Squire.class})
class TivadarsCrusadeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys Goblins on both battlefields and spares non-Goblins")
    void destroysAllGoblins() {
        harness.addToBattlefield(player1, new GoblinHero());
        harness.addToBattlefield(player2, new GoblinHero());
        harness.addToBattlefield(player2, new Squire());

        harness.castFromHand(player1, new TivadarsCrusade(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Hero");
        harness.assertNotOnBattlefield(player2, "Goblin Hero");
        harness.assertInGraveyard(player1, "Goblin Hero");
        harness.assertInGraveyard(player2, "Goblin Hero");
        harness.assertOnBattlefield(player2, "Squire");
        harness.assertNotInGraveyard(player2, "Squire");
        harness.assertInGraveyard(player1, "Tivadar's Crusade");
    }
}
