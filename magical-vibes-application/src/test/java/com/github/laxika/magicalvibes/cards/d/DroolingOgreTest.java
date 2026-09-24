package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DroolingOgre.class, DarksteelIngot.class, CrazedGoblin.class})
class DroolingOgreTest extends BaseCardTest {

    @Test
    @DisplayName("The artifact spell's caster gains control of Drooling Ogre")
    void artifactSpellCasterGainsControl() {
        harness.addToBattlefield(player1, new DroolingOgre());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new DarksteelIngot(), "{3}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Drooling Ogre");
        harness.assertOnBattlefield(player2, "Drooling Ogre");
    }

    @Test
    @DisplayName("The control change lasts indefinitely")
    void controlChangeLastsIndefinitely() {
        harness.addToBattlefield(player1, new DroolingOgre());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new DarksteelIngot(), "{3}");
        resolveAllTriggers();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Drooling Ogre");
    }

    @Test
    @DisplayName("A nonartifact spell does not trigger the control change")
    void nonartifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DroolingOgre());

        harness.castFromHand(player1, new CrazedGoblin(), "{R}");

        harness.assertOnBattlefield(player1, "Drooling Ogre");
        harness.assertNotOnBattlefield(player2, "Drooling Ogre");
    }
}
