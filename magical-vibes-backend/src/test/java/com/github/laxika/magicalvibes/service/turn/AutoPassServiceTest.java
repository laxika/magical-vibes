package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.cards.c.ColossusOfSardia;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AutoPassServiceTest extends BaseCardTest {

    @Nested
    @DisplayName("resolveAutoPass")
    class ResolveAutoPass {

        @Test
        @DisplayName("Returns immediately when game is not running")
        void returnsImmediatelyWhenGameNotRunning() {
            gd.status = GameStatus.FINISHED;
            TurnStep stepBefore = gd.currentStep;

            AutoPassService svc = new AutoPassService(
                    gqs, harness.getGameBroadcastService(),
                    harness.getTriggerCollectionService(),
                    harness.getStackResolutionService());
            svc.resolveAutoPass(gd, ignored -> {});

            // Step should not have changed
            assertThat(gd.currentStep).isEqualTo(stepBefore);
        }

        @Test
        @DisplayName("Stops when stack is non-empty")
        void stopsWhenStackIsNonEmpty() {
            harness.setHand(player1, List.of());
            harness.setHand(player2, List.of());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // Put something on the stack
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new GrizzlyBears(),
                    player1.getId(),
                    "Test trigger",
                    List.of()
            ));

            AutoPassService svc = new AutoPassService(
                    gqs, harness.getGameBroadcastService(),
                    harness.getTriggerCollectionService(),
                    harness.getStackResolutionService());
            svc.resolveAutoPass(gd, ignored -> {});

            // Should not advance — stack is non-empty, players must explicitly pass
            assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        }
    }

    @Nested
    @DisplayName("hasInstantSpeedActivatedAbility")
    class HasInstantSpeedActivatedAbility {

        @Test
        @DisplayName("Returns false when battlefield is empty")
        void returnsFalseWhenBattlefieldEmpty() {
            AutoPassService svc = new AutoPassService(
                    gqs, harness.getGameBroadcastService(),
                    harness.getTriggerCollectionService(),
                    harness.getStackResolutionService());

            assertThat(svc.hasInstantSpeedActivatedAbility(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("Returns false for vanilla creature with no abilities")
        void returnsFalseForVanillaCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());

            AutoPassService svc = new AutoPassService(
                    gqs, harness.getGameBroadcastService(),
                    harness.getTriggerCollectionService(),
                    harness.getStackResolutionService());

            assertThat(svc.hasInstantSpeedActivatedAbility(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("Returns false for creature with only upkeep-only activated ability")
        void returnsFalseForUpkeepOnlyAbility() {
            // Colossus of Sardia has an upkeep-only activated ability
            harness.addToBattlefield(player1, new ColossusOfSardia());

            AutoPassService svc = new AutoPassService(
                    gqs, harness.getGameBroadcastService(),
                    harness.getTriggerCollectionService(),
                    harness.getStackResolutionService());

            assertThat(svc.hasInstantSpeedActivatedAbility(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("Does not consider opponent's permanents")
        void doesNotConsiderOpponentPermanents() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            AutoPassService svc = new AutoPassService(
                    gqs, harness.getGameBroadcastService(),
                    harness.getTriggerCollectionService(),
                    harness.getStackResolutionService());

            assertThat(svc.hasInstantSpeedActivatedAbility(gd, player1.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("resolveAutoPassCombatTriggers")
    class ResolveAutoPassCombatTriggers {

        @Test
        @DisplayName("Returns immediately when stack is empty")
        void returnsImmediatelyWhenStackEmpty() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.COMBAT_DAMAGE);
            gd.stack.clear();

            AutoPassService svc = new AutoPassService(
                    gqs, harness.getGameBroadcastService(),
                    harness.getTriggerCollectionService(),
                    harness.getStackResolutionService());

            svc.resolveAutoPassCombatTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Returns immediately when game is finished")
        void returnsImmediatelyWhenGameFinished() {
            gd.status = GameStatus.FINISHED;
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new GrizzlyBears(),
                    player1.getId(),
                    "Test trigger",
                    List.of()
            ));

            AutoPassService svc = new AutoPassService(
                    gqs, harness.getGameBroadcastService(),
                    harness.getTriggerCollectionService(),
                    harness.getStackResolutionService());

            svc.resolveAutoPassCombatTriggers(gd);

            // Stack untouched since game is finished
            assertThat(gd.stack).hasSize(1);
        }
    }
}
