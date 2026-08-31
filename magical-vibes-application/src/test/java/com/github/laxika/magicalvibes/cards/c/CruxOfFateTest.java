package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrimstoneDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class CruxOfFateTest extends BaseCardTest {

    private void castCruxOfFate(int mode) {
        harness.setHand(player1, List.of(new CruxOfFate()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys all Dragon creatures and spares non-Dragon creatures")
    void destroysDragons() {
        harness.addToBattlefield(player1, new BrimstoneDragon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BrimstoneDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castCruxOfFate(0);

        harness.assertNotOnBattlefield(player1, "Brimstone Dragon");
        harness.assertNotOnBattlefield(player2, "Brimstone Dragon");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys all non-Dragon creatures and spares Dragons")
    void destroysNonDragons() {
        harness.addToBattlefield(player1, new BrimstoneDragon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BrimstoneDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castCruxOfFate(1);

        harness.assertOnBattlefield(player1, "Brimstone Dragon");
        harness.assertOnBattlefield(player2, "Brimstone Dragon");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
