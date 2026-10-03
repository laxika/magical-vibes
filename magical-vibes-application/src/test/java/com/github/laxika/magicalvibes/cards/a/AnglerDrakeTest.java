package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnglerDrake.class, Colossapede.class})
class AnglerDrakeTest extends BaseCardTest {

    private void castAnglerDrake() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AnglerDrake(), "{4}{U}{U}");
    }

    @Nested
    @DisplayName("ETB may bounce a creature")
    @CardUsed({AnglerDrake.class, Colossapede.class})
    class EtbMayBounce {

        @Test
        @DisplayName("Accepting bounces target creature to its owner's hand")
        void acceptingBouncesCreature() {
            harness.addToBattlefield(player2, new Colossapede());
            UUID targetId = harness.getPermanentId(player2, "Colossapede");
            castAnglerDrake();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, targetId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player2, "Colossapede");
            harness.assertInHand(player2, "Colossapede");
        }

        @Test
        @DisplayName("Declining leaves the target creature on the battlefield")
        void decliningDoesNotBounce() {
            harness.addToBattlefield(player2, new Colossapede());
            UUID targetId = harness.getPermanentId(player2, "Colossapede");
            castAnglerDrake();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, targetId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player2, "Colossapede");
        }

        @Test
        @DisplayName("The controller may return a friendly creature")
        void returnsFriendlyCreature() {
            harness.addToBattlefield(player1, new Colossapede());
            UUID targetId = harness.getPermanentId(player1, "Colossapede");
            castAnglerDrake();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, targetId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Colossapede");
            harness.assertInHand(player1, "Colossapede");
            harness.assertOnBattlefield(player1, "Angler Drake");
        }

        @Test
        @DisplayName("Angler Drake remains on the battlefield after the bounce resolves")
        void anglerDrakeEnters() {
            harness.addToBattlefield(player2, new Colossapede());
            UUID targetId = harness.getPermanentId(player2, "Colossapede");
            castAnglerDrake();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, targetId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Angler Drake");
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({AnglerDrake.class})
    class TargetingRestrictions {

        @Test
        @DisplayName("Accepting the ability can return Angler Drake itself")
        void canReturnItself() {
            castAnglerDrake();
            harness.passBothPriorities();
            UUID drakeId = harness.getPermanentId(player1, "Angler Drake");
            harness.handlePermanentChosen(player1, drakeId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Angler Drake");
            harness.assertInHand(player1, "Angler Drake");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("With no other creature, the drake itself is the only legal target")
        void canTargetItselfWhenNoOtherCreature() {
            // "return target creature" has no 'another' clause, so the drake is a legal target for
            // its own ETB. With no other creature present it is the only choice; declining is fine.
            castAnglerDrake();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.PermanentChoice.class);
            PendingInteraction.PermanentChoice choice =
                    (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
            assertThat(choice.validPermanentIds())
                    .containsExactly(harness.getPermanentId(player1, "Angler Drake"));
        }
    }
}
