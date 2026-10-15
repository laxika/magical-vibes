package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.InformationDealer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtboundPrimoc.class, InformationDealer.class, ElvishWarrior.class, Cloudshift.class})
class ThoughtboundPrimocTest extends BaseCardTest {

    @Test
    @DisplayName("The player with the most Wizards gains control during upkeep")
    void playerWithMostWizardsGainsControl() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new InformationDealer());
        harness.addToBattlefield(player2, new InformationDealer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("The controller keeps Thoughtbound Primoc when they control the most Wizards")
    void controllerKeepsControlWhenTheyHaveMostWizards() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player1, new InformationDealer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("Non-Wizard creatures do not count toward the control change")
    void nonWizardsDoNotCount() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("Thoughtbound Primoc does not change control when Wizard counts are tied")
    void noChangeOnTie() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player1, new InformationDealer());
        harness.addToBattlefield(player2, new InformationDealer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("The upkeep trigger does nothing if Wizard counts become tied before resolution")
    void noChangeWhenCountsTieBeforeResolution() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new InformationDealer());
        harness.addToBattlefield(player2, new InformationDealer());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new InformationDealer());
        harness.addToBattlefield(player1, new InformationDealer());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("The ability triggers only during the controller's upkeep")
    void doesNotTriggerDuringAnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new InformationDealer());
        harness.addToBattlefield(player2, new InformationDealer());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("A tie at upkeep prevents a trigger even if a player later takes the lead")
    void noTriggerWhenInitiallyTied() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player1, new InformationDealer());
        harness.addToBattlefield(player2, new InformationDealer());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player2, new InformationDealer());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("The player with the most Wizards at resolution gains control")
    void leaderIsDeterminedAtResolution() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player1, new InformationDealer());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player2, new InformationDealer());
        harness.addToBattlefield(player2, new InformationDealer());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("Control persists until an upkeep trigger resolves for the new controller")
    void newControllerCanLoseControlOnTheirUpkeep() {
        harness.addToBattlefield(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new InformationDealer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Thoughtbound Primoc");

        harness.addToBattlefield(player1, new InformationDealer());
        harness.addToBattlefield(player1, new InformationDealer());
        harness.assertOnBattlefield(player2, "Thoughtbound Primoc");

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("An old upkeep trigger cannot gain control of a blinked Primoc")
    void oldTriggerDoesNotAffectReturnedPrimoc() {
        var original = harness.addToBattlefieldAndReturn(player1, new ThoughtboundPrimoc());
        harness.addToBattlefield(player2, new InformationDealer());
        harness.setHand(player1, List.of(new Cloudshift()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, original.getId());

        assertThat(findPermanent(player1, "Thoughtbound Primoc").getId()).isNotEqualTo(original.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtbound Primoc");
        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
    }
}
