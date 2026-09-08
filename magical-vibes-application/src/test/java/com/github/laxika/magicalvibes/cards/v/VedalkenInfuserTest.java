package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VedalkenInfuserTest extends BaseCardTest {

    // ===== Upkeep triggered ability =====

    @Test
    @DisplayName("Upkeep trigger may put a charge counter on target artifact")
    void upkeepTriggerMayPutChargeCounterOnArtifact() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may ability does not put a charge counter")
    void decliningMayAbilityDoesNotPutCounter() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target opponent's artifact")
    void canTargetOpponentArtifact() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent opponentArtifact = addReadyArtifact(player2);

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(opponentArtifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeep triggers accumulate charge counters on same artifact")
    void multipleUpkeepTriggersAccumulateCounters() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        // First upkeep
        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Second upkeep
        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep ability does not trigger when no legal artifact target exists")
    void noTriggerWhenNoArtifacts() {
        Permanent infuser = addReadyInfuser(player1);

        triggerUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addReadyInfuser(Player player) {
        VedalkenInfuser card = new VedalkenInfuser();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        LeoninScimitar card = new LeoninScimitar();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
