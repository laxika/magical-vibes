package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CopperCarapace;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VedalkenInfuser.class, CopperCarapace.class})
class VedalkenInfuserTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may put a charge counter on target artifact")
    void upkeepTriggerMayPutChargeCounterOnArtifact() {
        addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may ability does not put a charge counter")
    void decliningMayAbilityDoesNotPutCounter() {
        addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target opponent's artifact")
    void canTargetOpponentArtifact() {
        addReadyInfuser(player1);
        Permanent opponentArtifact = addReadyArtifact(player2);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(opponentArtifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeep triggers accumulate charge counters on same artifact")
    void multipleUpkeepTriggersAccumulateCounters() {
        addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        // First upkeep
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Second upkeep
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep ability is not put on the stack when no legal artifact target exists")
    void noStackEntryWhenNoArtifacts() {
        addReadyInfuser(player1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("A target that leaves the battlefield makes the ability fail to resolve")
    void targetLeavingBattlefieldPreventsCounter() {
        addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even after its source leaves the battlefield")
    void sourceLeavingBattlefieldDoesNotPreventCounter() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(infuser);
        gd.playerGraveyards.get(player1.getId()).add(infuser.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    private Permanent addReadyInfuser(Player player) {
        return addCreatureReady(player, new VedalkenInfuser());
    }

    private Permanent addReadyArtifact(Player player) {
        return addCreatureReady(player, new CopperCarapace());
    }
}
