package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class WitheringCurseTest extends BaseCardTest {

    

    @Test
    @DisplayName("Without life gained, all creatures get -2/-2 (kills the 2/2, spares the 3/3)")
    void withoutLifeGainAppliesMinusTwoMinusTwo() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent giant = new Permanent(new HillGiant());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("If you gained life this turn, destroys all creatures instead (kills the 3/3 too)")
    void withLifeGainDestroysAllCreatures() {
        Permanent giant = new Permanent(new HillGiant());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
