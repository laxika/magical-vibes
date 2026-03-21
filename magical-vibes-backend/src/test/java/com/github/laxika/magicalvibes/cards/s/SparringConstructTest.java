package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SparringConstructTest extends BaseCardTest {

    /**
     * Sets up combat where Sparring Construct (player1) attacks and is blocked by a 3/3 creature (player2).
     * Sparring Construct will die from combat damage.
     */
    private void setupCombatWhereSparringConstructDies() {
        Permanent constructPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sparring Construct"))
                .findFirst().orElseThrow();
        constructPerm.setSummoningSick(false);
        constructPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Sparring Construct dies, controller is prompted to choose a target creature they control")
        void deathTriggerPromptsForTarget() {
            harness.addToBattlefield(player1, new SparringConstruct());
            harness.addToBattlefield(player1, new GrizzlyBears());
            setupCombatWhereSparringConstructDies();

            harness.passBothPriorities(); // Combat damage — Sparring Construct dies

            GameData gd = harness.getGameData();

            // Sparring Construct should be dead
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Sparring Construct"));

            // Controller should be prompted to choose a target creature
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Puts a +1/+1 counter on target creature you control")
        void putsCounterOnTargetCreature() {
            harness.addToBattlefield(player1, new SparringConstruct());
            harness.addToBattlefield(player1, new GrizzlyBears());

            UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

            setupCombatWhereSparringConstructDies();
            harness.passBothPriorities(); // Combat damage — Sparring Construct dies

            // Choose the Grizzly Bears
            harness.handlePermanentChosen(player1, bearId);

            // Triggered ability should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

            // Resolve the triggered ability
            harness.passBothPriorities();

            // Grizzly Bears should have 1 +1/+1 counter
            Permanent bearsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(bearId))
                    .findFirst().orElseThrow();
            assertThat(bearsPerm.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(bearsPerm.getEffectivePower()).isEqualTo(3);
            assertThat(bearsPerm.getEffectiveToughness()).isEqualTo(3);
        }

        @Test
        @DisplayName("Cannot target opponent's creature")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player1, new SparringConstruct());
            // No other creatures on player1's side

            setupCombatWhereSparringConstructDies();
            harness.passBothPriorities(); // Combat damage — Sparring Construct dies

            GameData gd = harness.getGameData();

            // No valid targets — trigger should be skipped
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
        }

        @Test
        @DisplayName("Death trigger skips when no creatures survive (Wrath of God)")
        void deathTriggerSkipsWithNoCreatures() {
            harness.addToBattlefield(player1, new SparringConstruct());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — all creatures die

            GameData gd = harness.getGameData();

            // All creatures dead — no valid targets for "creature you control"
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
        }
    }
}
