package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class DeathFrenzyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2 and gains life for each creature that dies")
    void weakensAllCreaturesAndGainsLifeForEachDeath() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        castDeathFrenzy();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Gains life when a creature dies later in the same turn")
    void triggersForLaterCreatureDeath() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLife(player1, 20);

        castDeathFrenzy();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Hill Giant"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("The delayed trigger expires at the end of the turn")
    void delayedTriggerExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLife(player1, 20);

        castDeathFrenzy();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Hill Giant"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    private void castDeathFrenzy() {
        harness.setHand(player1, List.of(new DeathFrenzy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
