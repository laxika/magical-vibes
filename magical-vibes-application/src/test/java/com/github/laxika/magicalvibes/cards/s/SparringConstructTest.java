package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CastDown;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({SparringConstruct.class, GrizzlyBears.class, WrathOfGod.class, CastDown.class})
class SparringConstructTest extends BaseCardTest {

    /**
     * Sets up combat where Sparring Construct (player1) attacks and is blocked by Grizzly Bears (player2).
     * Sparring Construct will die from combat damage.
     */
    private void setupCombatWhereSparringConstructDies() {
        Permanent constructPerm = findPermanent(player1, "Sparring Construct");
        constructPerm.setSummoningSick(false);
        constructPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Nested
    @DisplayName("Death trigger")
    @CardUsed({SparringConstruct.class, GrizzlyBears.class, WrathOfGod.class, CastDown.class})
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
            harness.assertInGraveyard(player1, "Sparring Construct");

            // Controller should be prompted to choose a target creature
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
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
            Permanent bearsPerm = findPermanent(player1, "Grizzly Bears");
            assertThat(bearsPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            assertThat(gameLogContains("no valid targets")).isTrue();
        }

        @Test
        @DisplayName("Death trigger skips when no creatures survive (Wrath of God)")
        void deathTriggerSkipsWithNoCreatures() {
            harness.addToBattlefield(player1, new SparringConstruct());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castAndResolveSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();

            // All creatures dead — no valid targets for "creature you control"
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            assertThat(gameLogContains("no valid targets")).isTrue();
        }

        @Test
        @DisplayName("A surviving artifact creature can receive the counter")
        void survivingConstructReceivesCounter() {
            harness.addToBattlefield(player1, new SparringConstruct());
            harness.addToBattlefield(player1, new SparringConstruct());
            Permanent survivor = findPermanents(player1, "Sparring Construct").get(1);
            setupCombatWhereSparringConstructDies();
            harness.passBothPriorities();

            harness.handlePermanentChosen(player1, survivor.getId());
            harness.passBothPriorities();

            assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(countPermanents(player1, "Sparring Construct")).isEqualTo(1);
        }

        @Test
        @DisplayName("The death trigger does nothing if its target dies before resolution")
        void targetDiesBeforeResolution() {
            harness.addToBattlefield(player1, new SparringConstruct());
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent target = findPermanent(player1, "Grizzly Bears");
            setupCombatWhereSparringConstructDies();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, target.getId());

            harness.setHand(player2, List.of(new CastDown()));
            harness.addMana(player2, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player2, 0, target.getId());
            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(findPermanent(player2, "Grizzly Bears")
                    .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }
    }
}
