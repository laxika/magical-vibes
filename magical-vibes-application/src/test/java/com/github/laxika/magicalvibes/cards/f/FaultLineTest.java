package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.ViashinoRunner;
import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({FaultLine.class, ViashinoRunner.class, Zephid.class, Island.class})
class FaultLineTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to each player and each creature without flying")
    void damagesPlayersAndNonFlyingCreatures() {
        harness.addToBattlefield(player1, new ViashinoRunner());
        harness.addToBattlefield(player1, new Zephid());
        harness.addToBattlefield(player2, new ViashinoRunner());
        harness.addToBattlefield(player2, new Zephid());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new FaultLine()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Runner");
        harness.assertOnBattlefield(player1, "Zephid");
        harness.assertNotOnBattlefield(player2, "Viashino Runner");
        harness.assertOnBattlefield(player2, "Zephid");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new ViashinoRunner());
        harness.setHand(player1, List.of(new FaultLine()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Viashino Runner");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
