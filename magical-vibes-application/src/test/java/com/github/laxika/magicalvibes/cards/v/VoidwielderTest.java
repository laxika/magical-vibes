package com.github.laxika.magicalvibes.cards.v;

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

class VoidwielderTest extends BaseCardTest {

    private void castVoidwielder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Voidwielder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
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
            castVoidwielder();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB may from stack -> may prompt
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, bearsId);

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInHand(player2, "Grizzly Bears");
            harness.assertOnBattlefield(player1, "Voidwielder");
        }

        @Test
        @DisplayName("Declining leaves the target creature on the battlefield")
        void decliningDoesNotBounce() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castVoidwielder();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB may from stack -> may prompt
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("With no other creature, Voidwielder itself is a legal target")
        void canTargetItselfWhenNoOtherCreature() {
            // "return target creature" has no 'another' clause, so Voidwielder is a legal target for
            // its own ETB. With no other creature present it is the only choice.
            castVoidwielder();
            harness.passBothPriorities(); // resolve creature spell -> Voidwielder enters
            harness.passBothPriorities(); // resolve ETB may -> may prompt

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }
    }
}
