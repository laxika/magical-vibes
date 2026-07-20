package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnglerDrakeTest extends BaseCardTest {

    private void castAnglerDrake() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AnglerDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    @Nested
    @DisplayName("ETB may bounce a creature")
    class EtbMayBounce {

        @Test
        @DisplayName("Accepting bounces target creature to its owner's hand")
        void acceptingBouncesCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            castAnglerDrake();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB may from stack -> may prompt
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, bearsId);

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Declining leaves the target creature on the battlefield")
        void decliningDoesNotBounce() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castAnglerDrake();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB may from stack -> may prompt
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Angler Drake still enters after the bounce resolves")
        void anglerDrakeEnters() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            castAnglerDrake();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, bearsId);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Angler Drake"));
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("With no other creature, the drake itself is the only legal target")
        void canTargetItselfWhenNoOtherCreature() {
            // "return target creature" has no 'another' clause, so the drake is a legal target for
            // its own ETB. With no other creature present it is the only choice; declining is fine.
            castAnglerDrake();
            harness.passBothPriorities(); // resolve creature spell -> Angler Drake enters
            harness.passBothPriorities(); // resolve ETB may -> may prompt (drake is a legal target)

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }
    }
}
