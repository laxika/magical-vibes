package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class MassCalcifyTest extends BaseCardTest {

    private void castMassCalcify() {
        harness.setHand(player1, List.of(new MassCalcify()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys all nonwhite creatures on both battlefields")
    void destroysNonwhiteCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMassCalcify();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves white creatures on the battlefield")
    void leavesWhiteCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMassCalcify();

        harness.assertOnBattlefield(player1, "Elite Vanguard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
