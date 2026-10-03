package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.p.PiratesCutlass;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({DreamcallerSiren.class, DaringSaboteur.class, JungleDelver.class, Forest.class, AirElemental.class, PiratesCutlass.class})
class DreamcallerSirenTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB with another Pirate")
    @CardUsed({DreamcallerSiren.class, DaringSaboteur.class, JungleDelver.class, Forest.class})
    class EtbWithAnotherPirate {

        @Test
        @DisplayName("ETB triggers when you control another Pirate — taps two targets chosen at trigger time")
        void etbTapsTwoTargets() {
            harness.addToBattlefield(player1, new DaringSaboteur()); // Pirate

            Permanent bears1 = harness.addToBattlefieldAndReturn(player2, new JungleDelver());
            Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new JungleDelver());
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

            Permanent bears = harness.addToBattlefieldAndReturn(player2, new JungleDelver());
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
            harness.addToBattlefield(player2, new JungleDelver());
            UUID bearsId = harness.getPermanentId(player2, "Jungle Delver");

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
            harness.addToBattlefield(player2, new JungleDelver());
            UUID bearsId = harness.getPermanentId(player2, "Jungle Delver");

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
            harness.addToBattlefield(player2, new JungleDelver());
            harness.addToBattlefield(player2, new Forest());

            castDreamcallerSiren();
            harness.passBothPriorities(); // resolve creature spell — trigger-time target prompts

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();

            UUID landId = findPermanent(player2, "Forest").getId();
            assertThatThrownBy(() -> harness.handlePermanentChosen(player1, landId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid permanent");
        }
    }

    @Nested
    @DisplayName("ETB without another Pirate")
    @CardUsed({DreamcallerSiren.class, DaringSaboteur.class})
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

    @Test
    @DisplayName("ETB does nothing if the other Pirate is removed before resolution")
    void etbFizzlesWhenAnotherPirateRemoved() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new JungleDelver());

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

    @Nested
    @DisplayName("Blocking restriction")
    @CardUsed({DreamcallerSiren.class, AirElemental.class, JungleDelver.class})
    class BlockingRestriction {

        @Test
        @DisplayName("Can block a creature with flying")
        void canBlockFlyingCreature() {
            Permanent sirenPerm = harness.addToBattlefieldAndReturn(player2, new DreamcallerSiren());
            sirenPerm.setSummoningSick(false);

            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new AirElemental());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);

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
            Permanent sirenPerm = harness.addToBattlefieldAndReturn(player2, new DreamcallerSiren());
            sirenPerm.setSummoningSick(false);

            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.beginBlockerDeclarationInput();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can only block creatures with flying");
        }
    }

    @Test
    void canChooseZeroTargets() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleDelver());

        castDreamcallerSiren();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anotherDreamcallerSirenEnablesTheTrigger() {
        harness.addToBattlefield(player1, new DreamcallerSiren());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleDelver());

        castDreamcallerSiren();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void nonPirateCreatureDoesNotEnableTheTrigger() {
        harness.addToBattlefield(player1, new JungleDelver());

        castDreamcallerSiren();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Dreamcaller Siren");
    }

    @Test
    void enteringWithoutBeingCastStillTriggers() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleDelver());

        harness.enterBattlefieldAndReturn(player1, new DreamcallerSiren());
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItselfAndANoncreatureArtifact() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new PiratesCutlass());

        castDreamcallerSiren();
        harness.passBothPriorities();
        Permanent siren = findPermanent(player1, "Dreamcaller Siren");
        harness.handlePermanentChosen(player1, siren.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(siren.isTapped()).isTrue();
        assertThat(equipment.isTapped()).isTrue();
    }

    @Test
    void cannotChooseTheSamePermanentTwice() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleDelver());

        castDreamcallerSiren();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapsRemainingTargetWhenOneTargetLeaves() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new JungleDelver());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new JungleDelver());

        castDreamcallerSiren();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void abilityResolvesWhenSirenLeavesAndAnotherPirateRemains() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleDelver());

        castDreamcallerSiren();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Dreamcaller Siren"));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canBeCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        castDreamcallerSiren();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dreamcaller Siren");
        assertThat(gd.stack).isEmpty();
    }

    private void castDreamcallerSiren() {
        harness.castFromHand(player1, new DreamcallerSiren(), "{2}{U}{U}");
    }
}
