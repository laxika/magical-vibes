package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.k.KazanduStomper;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuulDrazMucklord.class, GnarlidColony.class, KazanduStomper.class})
class GuulDrazMucklordTest extends BaseCardTest {

    @Test
    @DisplayName("When Guul Draz Mucklord dies, controller is prompted to choose a creature they control")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new GuulDrazMucklord());
        harness.addToBattlefield(player1, new GnarlidColony());
        setupCombatWhereMucklordDies();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Guul Draz Mucklord");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("When Guul Draz Mucklord dies, it puts a +1/+1 counter on the chosen creature")
    void putsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new GuulDrazMucklord());
        harness.addToBattlefield(player1, new GnarlidColony());
        UUID colonyId = harness.getPermanentId(player1, "Gnarlid Colony");

        setupCombatWhereMucklordDies();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, colonyId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        Permanent colony = findPermanent(player1, "Gnarlid Colony");
        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(colony.getEffectivePower()).isEqualTo(3);
        assertThat(colony.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("When no creature you control survives, the death trigger has no valid target")
    void deathTriggerHasNoValidTarget() {
        harness.addToBattlefield(player1, new GuulDrazMucklord());
        setupCombatWhereMucklordDies();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("no valid targets"));
    }

    @Test
    @DisplayName("The death trigger offers only surviving creatures its controller controls")
    void deathTriggerExcludesOpposingCreatures() {
        harness.addToBattlefield(player1, new GuulDrazMucklord());
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new GnarlidColony());
        harness.addToBattlefield(player2, new GnarlidColony());
        setupCombatWhereMucklordDies();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(colony.getId());
    }

    @Test
    @DisplayName("The death trigger does not give a counter to another creature when its target leaves")
    void targetLeavingBeforeResolutionDoesNotRedirectCounter() {
        harness.addToBattlefield(player1, new GuulDrazMucklord());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GnarlidColony());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GnarlidColony());
        setupCombatWhereMucklordDies();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void setupCombatWhereMucklordDies() {
        Permanent mucklord = findPermanent(player1, "Guul Draz Mucklord");
        mucklord.setSummoningSick(false);
        mucklord.setAttacking(true);

        Permanent blockerPermanent = harness.addToBattlefieldAndReturn(player2, new KazanduStomper());
        blockerPermanent.setSummoningSick(false);
        blockerPermanent.setBlocking(true);
        blockerPermanent.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
