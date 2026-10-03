package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiritFlagshipVessel.class, GrizzlyBears.class, DarksteelRelic.class})
class InspiritFlagshipVesselTest extends BaseCardTest {

    @Test
    @DisplayName("Station adds charge counters equal to the tapped creature's power")
    void stationAddsChargeCounters() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InspiritFlagshipVessel());
        Permanent creature = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(vessel.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The beginning-of-combat modal puts a +1/+1 counter on another artifact")
    void putsPlusOneCounterOnTargetArtifact() {
        addVesselAndArtifact();
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).get(1);

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a +1/+1 counter on it");

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The beginning-of-combat modal can put two charge counters on another artifact")
    void putsChargeCountersOnTargetArtifact() {
        addVesselAndArtifact();
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).get(1);

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put two charge counters on it");

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Other artifacts you control have hexproof and indestructible")
    void protectsOtherControlledArtifactsOnly() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InspiritFlagshipVessel());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());

        assertThat(gqs.hasKeyword(gd, vessel, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, vessel, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingArtifact, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingArtifact, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The beginning-of-combat target must be another artifact you control")
    void targetMustBeAnotherControlledArtifact() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InspiritFlagshipVessel());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());

        advanceToBeginningOfCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, vessel.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void addVesselAndArtifact() {
        harness.addToBattlefieldAndReturn(player1, new InspiritFlagshipVessel());
        harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
