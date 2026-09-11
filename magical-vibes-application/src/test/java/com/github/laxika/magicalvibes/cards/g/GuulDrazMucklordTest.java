package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({GuulDrazMucklord.class, GrizzlyBears.class})
class GuulDrazMucklordTest extends BaseCardTest {

    @Test
    @DisplayName("When Guul Draz Mucklord dies, controller is prompted to choose a creature they control")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new GuulDrazMucklord());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setupCombatWhereMucklordDies();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Guul Draz Mucklord");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("When Guul Draz Mucklord dies, it puts a +1/+1 counter on the chosen creature")
    void putsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new GuulDrazMucklord());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        setupCombatWhereMucklordDies();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(bearId))
                .findFirst().orElseThrow();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("When no creature you control survives, the death trigger has no valid target")
    void deathTriggerHasNoValidTarget() {
        harness.addToBattlefield(player1, new GuulDrazMucklord());
        setupCombatWhereMucklordDies();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("no valid targets"));
    }

    private void setupCombatWhereMucklordDies() {
        Permanent mucklord = findPermanent(player1, "Guul Draz Mucklord");
        mucklord.setSummoningSick(false);
        mucklord.setAttacking(true);

        GrizzlyBears blocker = new GrizzlyBears();
        blocker.setPower(3);
        blocker.setToughness(3);
        Permanent blockerPermanent = new Permanent(blocker);
        blockerPermanent.setSummoningSick(false);
        blockerPermanent.setBlocking(true);
        blockerPermanent.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blockerPermanent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
