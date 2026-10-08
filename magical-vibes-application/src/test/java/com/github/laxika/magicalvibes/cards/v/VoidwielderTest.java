package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Voidwielder.class, AxebaneStag.class})
class VoidwielderTest extends BaseCardTest {

    private void castVoidwielder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Voidwielder(), "{4}{U}");
    }

    @Nested
    @DisplayName("ETB may bounce a creature")
    @CardUsed({Voidwielder.class, AxebaneStag.class})
    class EtbMayBounce {

        @Test
        @DisplayName("Accepting bounces target creature to its owner's hand")
        void acceptingBouncesCreature() {
            harness.addToBattlefield(player2, new AxebaneStag());
            UUID targetId = harness.getPermanentId(player2, "Axebane Stag");
            castVoidwielder();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, targetId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player2, "Axebane Stag");
            harness.assertInHand(player2, "Axebane Stag");
            harness.assertOnBattlefield(player1, "Voidwielder");
        }

        @Test
        @DisplayName("Declining leaves the target creature on the battlefield")
        void decliningDoesNotBounce() {
            harness.addToBattlefield(player2, new AxebaneStag());
            UUID targetId = harness.getPermanentId(player2, "Axebane Stag");
            castVoidwielder();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, targetId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player2, "Axebane Stag");
        }
    }

    @Test
    @DisplayName("Accepting can return Voidwielder itself to hand")
    void acceptingCanReturnItself() {
        castVoidwielder();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Voidwielder"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Voidwielder");
        harness.assertInHand(player1, "Voidwielder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns a creature to its owner rather than its controller")
    void returnsToOwnerRatherThanController() {
        AxebaneStag target = new AxebaneStag();
        target.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, target);
        castVoidwielder();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Axebane Stag"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Axebane Stag");
        harness.assertInHand(player1, "Axebane Stag");
        harness.assertNotInHand(player2, "Axebane Stag");
    }

    @Test
    @DisplayName("The trigger still bounces its target after Voidwielder leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AxebaneStag());
        castVoidwielder();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, findPermanent(player1, "Voidwielder")));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Voidwielder");
        harness.assertNotOnBattlefield(player2, "Axebane Stag");
        harness.assertInHand(player2, "Axebane Stag");
    }

    @Test
    @DisplayName("A target that leaves before resolution is not returned from the graveyard")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AxebaneStag());
        castVoidwielder();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Axebane Stag");
        harness.assertNotInHand(player2, "Axebane Stag");
        harness.assertOnBattlefield(player1, "Voidwielder");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({Voidwielder.class})
    class TargetingRestrictions {

        @Test
        @DisplayName("With no other creature, Voidwielder itself is a legal target")
        void canTargetItselfWhenNoOtherCreature() {
            // "return target creature" has no 'another' clause, so Voidwielder is a legal target for
            // its own ETB. With no other creature present it is the only choice.
            castVoidwielder();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.PermanentChoice.class);
        }
    }
}
