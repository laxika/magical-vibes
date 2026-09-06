package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ThoughtboundPrimoc.class, FugitiveWizard.class, GrizzlyBears.class})
class ThoughtboundPrimocTest extends BaseCardTest {

    @Test
    @DisplayName("The player with the most Wizards gains control during upkeep")
    void playerWithMostWizardsGainsControl() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("Non-Wizard creatures do not count toward the control change")
    void nonWizardsDoNotCount() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("Thoughtbound Primoc does not change control when Wizard counts are tied")
    void noChangeOnTie() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("The upkeep trigger does nothing if Wizard counts become tied before resolution")
    void noChangeWhenCountsTieBeforeResolution() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }
}
