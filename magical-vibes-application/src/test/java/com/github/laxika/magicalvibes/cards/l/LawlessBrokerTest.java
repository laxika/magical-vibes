package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

class LawlessBrokerTest extends BaseCardTest {

    private void setupCombatWhereLawlessBrokerDies() {
        Permanent brokerPerm = findPermanent(player1, "Lawless Broker");
        brokerPerm.setSummoningSick(false);
        brokerPerm.setAttacking(true);

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
        @DisplayName("When Lawless Broker dies, controller is prompted to choose a target creature they control")
        void deathTriggerPromptsForTarget() {
            harness.addToBattlefield(player1, new LawlessBroker());
            harness.addToBattlefield(player1, new GrizzlyBears());
            setupCombatWhereLawlessBrokerDies();

            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            harness.assertInGraveyard(player1, "Lawless Broker");
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                    .isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Puts a +1/+1 counter on target creature you control")
        void putsCounterOnTargetCreature() {
            harness.addToBattlefield(player1, new LawlessBroker());
            harness.addToBattlefield(player1, new GrizzlyBears());

            UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

            setupCombatWhereLawlessBrokerDies();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, bearId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

            harness.passBothPriorities();

            Permanent bearsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(bearId))
                    .findFirst().orElseThrow();
            assertThat(bearsPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(bearsPerm.getEffectivePower()).isEqualTo(3);
            assertThat(bearsPerm.getEffectiveToughness()).isEqualTo(3);
        }

        @Test
        @DisplayName("Cannot target opponent's creature")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player1, new LawlessBroker());

            setupCombatWhereLawlessBrokerDies();
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                    .anyMatch(log -> log.contains("no valid targets"));
        }

        @Test
        @DisplayName("Death trigger skips when no creatures survive")
        void deathTriggerSkipsWithNoCreatures() {
            harness.addToBattlefield(player1, new LawlessBroker());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                    .anyMatch(log -> log.contains("no valid targets"));
        }
    }
}
