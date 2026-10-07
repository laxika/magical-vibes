package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelSabotage.class, Millstone.class, GrizzlyBears.class, SpinEngine.class})
class SteelSabotageTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 1: Counter target artifact spell")
    @CardUsed({SteelSabotage.class, Millstone.class, GrizzlyBears.class, SpinEngine.class})
    class CounterMode {

        @Test
        @DisplayName("Counters an artifact creature spell")
        void countersArtifactCreatureSpell() {
            SpinEngine creature = new SpinEngine();
            harness.setHand(player1, List.of(creature));
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, creature.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Spin Engine");
            harness.assertNotOnBattlefield(player1, "Spin Engine");
            harness.assertInGraveyard(player2, "Steel Sabotage");
        }

        @Test
        @DisplayName("Counters target artifact spell")
        void countersArtifactSpell() {
            Millstone millstone = new Millstone();
            harness.setHand(player1, List.of(millstone));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castArtifact(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, millstone.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Millstone");
            harness.assertNotOnBattlefield(player1, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a creature spell with counter mode")
        void cannotTargetCreatureSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Steel Sabotage goes to graveyard after countering")
        void goesToGraveyardAfterResolving() {
            Millstone millstone = new Millstone();
            harness.setHand(player1, List.of(millstone));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castArtifact(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, millstone.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player2, "Steel Sabotage");
        }
    }

    @Nested
    @DisplayName("Mode 2: Return target artifact to its owner's hand")
    @CardUsed({SteelSabotage.class, Millstone.class, GrizzlyBears.class, SpinEngine.class})
    class BounceMode {

        @Test
        @DisplayName("Can return your own artifact creature to hand")
        void returnsOwnArtifactCreatureToHand() {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinEngine());
            harness.setHand(player1, List.of(new SteelSabotage()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, creature.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Spin Engine");
            harness.assertInHand(player1, "Spin Engine");
            harness.assertInGraveyard(player1, "Steel Sabotage");
        }

        @Test
        @DisplayName("Bounce mode cannot target an artifact spell on the stack")
        void cannotBounceArtifactSpell() {
            SpinEngine creature = new SpinEngine();
            harness.setHand(player1, List.of(creature));
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, creature.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Does not return the same card again after its target leaves the battlefield")
        void targetLeavesBattlefieldBeforeResolution() {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinEngine());
            harness.setHand(player1, List.of(new SteelSabotage()));
            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, creature.getId());
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 1, creature.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Spin Engine");
            assertThat(gd.playerHands.get(player1.getId()))
                    .filteredOn(card -> card.getId().equals(creature.getCard().getId()))
                    .hasSize(1);
            harness.assertInGraveyard(player1, "Steel Sabotage");
            harness.assertInGraveyard(player2, "Steel Sabotage");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Returns target artifact to its owner's hand")
        void returnsArtifactToHand() {
            Permanent millstonePermanent = harness.addToBattlefieldAndReturn(player1, new Millstone());

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.castInstant(player2, 0, 1, millstonePermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Millstone");
            harness.assertInHand(player1, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature with bounce mode")
        void cannotTargetNonArtifactCreature() {
            Permanent bearsPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Steel Sabotage goes to graveyard after bouncing")
        void goesToGraveyardAfterResolving() {
            Permanent millstonePermanent = harness.addToBattlefieldAndReturn(player1, new Millstone());

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.castInstant(player2, 0, 1, millstonePermanent.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player2, "Steel Sabotage");
        }
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new SteelSabotage()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 99, millstone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }
}
