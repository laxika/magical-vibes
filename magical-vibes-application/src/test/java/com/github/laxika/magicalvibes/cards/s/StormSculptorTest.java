package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormSculptor.class, RaptorCompanion.class})
class StormSculptorTest extends BaseCardTest {

    @Nested
    @CardUsed({StormSculptor.class, RaptorCompanion.class})
    @DisplayName("ETB bounce creature you control")
    class EtbBounce {

        @Test
        @DisplayName("ETB trigger goes on the stack when Storm Sculptor enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player1, new RaptorCompanion());
            castStormSculptor();
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Sculptor");
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        @DisplayName("ETB resolves: chosen creature is returned to owner's hand")
        void etbBouncesChosenCreature() {
            harness.addToBattlefield(player1, new RaptorCompanion());
            castStormSculptor();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger
            harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Raptor Companion"));

            harness.assertNotOnBattlefield(player1, "Raptor Companion");
            harness.assertInHand(player1, "Raptor Companion");
        }

        @Test
        @DisplayName("Storm Sculptor remains on battlefield after bouncing another creature")
        void sculptorRemainsAfterBounce() {
            harness.addToBattlefield(player1, new RaptorCompanion());
            castStormSculptor();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger
            harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Raptor Companion"));

            harness.assertOnBattlefield(player1, "Storm Sculptor");
        }

        @Test
        @DisplayName("Stack is empty after full resolution")
        void stackEmptyAfterResolution() {
            harness.addToBattlefield(player1, new RaptorCompanion());
            castStormSculptor();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger
            harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Raptor Companion"));

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Storm Sculptor must return itself when it is the only creature")
        void returnsItselfWhenAlone() {
            castStormSculptor();
            harness.passBothPriorities();
            UUID sculptorId = harness.getPermanentId(player1, "Storm Sculptor");
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                    .containsExactly(sculptorId);
            harness.handlePermanentChosen(player1, sculptorId);

            harness.assertNotOnBattlefield(player1, "Storm Sculptor");
            harness.assertInHand(player1, "Storm Sculptor");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("A creature that arrives after the trigger stacks can be chosen on resolution")
        void choosesFromCreaturesPresentAtResolution() {
            castStormSculptor();
            harness.passBothPriorities();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            UUID companionId = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion()).getId();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                    .containsExactlyInAnyOrder(companionId, harness.getPermanentId(player1, "Storm Sculptor"));
            harness.handlePermanentChosen(player1, companionId);

            harness.assertInHand(player1, "Raptor Companion");
            harness.assertOnBattlefield(player1, "Storm Sculptor");
        }

        @Test
        @DisplayName("A creature controlled by you but owned by the opponent returns to its owner")
        void returnsBorrowedCreatureToOwner() {
            RaptorCompanion borrowed = new RaptorCompanion();
            borrowed.setOwnerId(player2.getId());
            UUID companionId = harness.addToBattlefieldAndReturn(player1, borrowed).getId();
            castStormSculptor();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, companionId);

            harness.assertNotOnBattlefield(player1, "Raptor Companion");
            harness.assertInHand(player2, "Raptor Companion");
            harness.assertNotInHand(player1, "Raptor Companion");
            harness.assertOnBattlefield(player1, "Storm Sculptor");
        }
    }

    @Nested
    @CardUsed({StormSculptor.class, RaptorCompanion.class})
    @DisplayName("Resolution-time choice restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot choose opponent's creature")
        void cannotChooseOpponentCreature() {
            harness.addToBattlefield(player2, new RaptorCompanion());
            UUID opponentCreatureId = harness.getPermanentId(player2, "Raptor Companion");
            castStormSculptor();
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                    .containsExactly(harness.getPermanentId(player1, "Storm Sculptor"));
            assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreatureId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({StormSculptor.class, RaptorCompanion.class})
    @DisplayName("Can't be blocked")
    class CantBeBlocked {

        @Test
        @DisplayName("Storm Sculptor cannot be blocked")
        void cannotBeBlocked() {
            Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
            blockerPerm.setSummoningSick(false);

            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new StormSculptor());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.beginBlockerDeclarationInput();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be blocked");
        }

        @Test
        @DisplayName("Unblocked Storm Sculptor deals 3 damage to defending player")
        void dealsThreeDamageWhenUnblocked() {
            harness.setLife(player2, 20);

            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new StormSculptor());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }
    }

    private void castStormSculptor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StormSculptor()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
    }

}
