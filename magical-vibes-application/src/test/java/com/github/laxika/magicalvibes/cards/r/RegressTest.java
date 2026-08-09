package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class RegressTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target land permanent to its owner's hand")
    void returnsTargetLandToOwnersHand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player1, "Regress");
    }

    @Test
    @DisplayName("Fizzles if the target permanent leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Regress");
    }
}
