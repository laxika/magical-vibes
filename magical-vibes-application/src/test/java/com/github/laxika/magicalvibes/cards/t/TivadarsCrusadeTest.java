package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({TivadarsCrusade.class, GoblinRaider.class, GrizzlyBears.class})
class TivadarsCrusadeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys Goblins on both battlefields and spares non-Goblins")
    void destroysAllGoblins() {
        harness.addToBattlefield(player1, new GoblinRaider());
        harness.addToBattlefield(player2, new GoblinRaider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TivadarsCrusade()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Raider");
        harness.assertNotOnBattlefield(player2, "Goblin Raider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
