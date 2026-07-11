package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreamcallerSirenTest extends BaseCardTest {

    // ===== ETB with another Pirate =====

    @Nested
    @DisplayName("ETB with another Pirate")
    class EtbWithAnotherPirate {

        @Test
        @DisplayName("ETB triggers when you control another Pirate — taps two targets chosen at trigger time")
        void etbTapsTwoTargets() {
            harness.addToBattlefield(player1, new DaringSaboteur()); // Pirate

            Permanent bears1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            assertThat(bears1.isTapped()).isFalse();
            assertThat(bears2.isTapped()).isFalse();

            castDreamcallerSiren();
            harness.passBothPriorities(); // resolve creature spell — trigger-time target prompts
            harness.handlePermanentChosen(player1, bears1.getId());
            harness.handlePermanentChosen(player1, bears2.getId()); // max reached — trigger on stack
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears1.isTapped()).isTrue();
            assertThat(bears2.isTapped()).isTrue();
        }

        @Test
        @DisplayName("ETB triggers when you control another Pirate — taps one target, then stops")
        void etbTapsOneTarget() {
            harness.addToBattlefield(player1, new DaringSaboteur()); // Pirate

            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            assertThat(bears.isTapped()).isFalse();

            castDreamcallerSiren();
            harness.passBothPriorities(); // resolve creature spell — trigger-time target prompts
            harness.handlePermanentChosen(player1, bears.getId());
            harness.handlePermanentChosen(player1, player1.getId()); // choose self to stop at one target
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("ETB trigger goes on the stack once targets are chosen")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player1, new DaringSaboteur()); // Pirate
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            castDreamcallerSiren();
            harness.passBothPriorities(); // resolve creature spell — trigger-time target prompts

            // Casting never asked for a target — the choice happens now, as the
            // trigger is put on the stack (CR 603.3d)
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.PermanentChoice.class);

            harness.handlePermanentChosen(player1, bearsId);
            harness.handlePermanentChosen(player1, player1.getId()); // stop after one target

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dreamcaller Siren");
        }

        @Test
        @DisplayName("Creature enters battlefield with another Pirate")
        void creatureEntersWithAnotherPirate() {
            harness.addToBattlefield(player1, new DaringSaboteur());
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            castDreamcallerSiren();
            harness.passBothPriorities(); // resolve creature spell — trigger-time target prompts
            harness.handlePermanentChosen(player1, bearsId);
            harness.handlePermanentChosen(player1, player1.getId()); // stop after one target

            harness.assertOnBattlefield(player1, "Dreamcaller Siren");
        }

        @Test
        @DisplayName("Cannot choose a land as a trigger target")
        void cannotChooseLandTarget() {
            harness.addToBattlefield(player1, new DaringSaboteur()); // Pirate
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new Forest());

            castDreamcallerSiren();
            harness.passBothPriorities(); // resolve creature spell — trigger-time target prompts

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();

            UUID landId = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Forest"))
                    .findFirst().orElseThrow().getId();
            assertThatThrownBy(() -> harness.handlePermanentChosen(player1, landId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid permanent");
        }
    }

    // ===== ETB without another Pirate =====

    @Nested
    @DisplayName("ETB without another Pirate")
    class EtbWithoutAnotherPirate {

        @Test
        @DisplayName("ETB does NOT trigger without another Pirate (only self)")
        void etbDoesNotTriggerWithoutAnotherPirate() {
            castDreamcallerSiren();
            harness.passBothPriorities(); // resolve creature spell

            // No ETB trigger on the stack and no target prompt (intervening-if failed, CR 603.4)
            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();

            // Creature is still on the battlefield
            harness.assertOnBattlefield(player1, "Dreamcaller Siren");
        }

        @Test
        @DisplayName("ETB does NOT trigger when opponent controls a Pirate but you don't")
        void etbDoesNotTriggerWithOpponentPirate() {
            harness.addToBattlefield(player2, new DaringSaboteur());

            castDreamcallerSiren();
            harness.passBothPriorities(); // resolve creature spell

            // No ETB trigger
            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    // ===== Condition lost before resolution =====

    @Test
    @DisplayName("ETB does nothing if the other Pirate is removed before resolution")
    void etbFizzlesWhenAnotherPirateRemoved() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDreamcallerSiren();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompts
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, player1.getId()); // stop — ETB trigger on stack

        // Remove the other Pirate before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Daring Saboteur"));

        harness.passBothPriorities(); // resolve ETB trigger — condition no longer met

        // Target should NOT be tapped (ability does nothing)
        assertThat(bears.isTapped()).isFalse();
    }

    // ===== Blocking restriction =====

    @Nested
    @DisplayName("Blocking restriction")
    class BlockingRestriction {

        @Test
        @DisplayName("Can block a creature with flying")
        void canBlockFlyingCreature() {
            Permanent sirenPerm = new Permanent(new DreamcallerSiren());
            sirenPerm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(sirenPerm);

            Permanent atkPerm = new Permanent(new AirElemental());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.beginBlockerDeclarationInput();

            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

            assertThat(sirenPerm.isBlocking()).isTrue();
        }

        @Test
        @DisplayName("Cannot block a creature without flying")
        void cannotBlockNonFlyingCreature() {
            Permanent sirenPerm = new Permanent(new DreamcallerSiren());
            sirenPerm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(sirenPerm);

            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.beginBlockerDeclarationInput();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can only block creatures with flying");
        }
    }

    // ===== Helpers =====

    private void castDreamcallerSiren() {
        harness.setHand(player1, List.of(new DreamcallerSiren()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
    }
}
