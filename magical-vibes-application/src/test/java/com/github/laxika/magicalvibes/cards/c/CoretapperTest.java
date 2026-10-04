package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Coretapper.class, DarksteelCitadel.class, CrazedGoblin.class})
class CoretapperTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Coretapper puts a charge counter on target artifact")
    void tappingPutsChargeCounterOnTargetArtifact() {
        Permanent coretapper = addCreatureReady(player1, new Coretapper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(coretapper.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Coretapper puts two charge counters on target artifact")
    void sacrificingPutsTwoChargeCountersOnTargetArtifact() {
        addCreatureReady(player1, new Coretapper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Coretapper");
        harness.assertInGraveyard(player1, "Coretapper");
    }

    @Test
    @DisplayName("Coretapper can target an artifact an opponent controls")
    void canTargetOpponentsArtifact() {
        addCreatureReady(player1, new Coretapper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Coretapper cannot target a non-artifact permanent")
    void cannotTargetNonArtifactPermanent() {
        addCreatureReady(player1, new Coretapper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrazedGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }
    @Test
    @DisplayName("Summoning sickness prevents the tap ability")
    void cannotTapWhileSummoningSick() {
        Permanent coretapper = harness.addToBattlefieldAndReturn(player1, new Coretapper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(coretapper.isTapped()).isFalse();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick tapped Coretapper can be sacrificed for an opponent's artifact")
    void canSacrificeWhileSummoningSickAndTapped() {
        Permanent coretapper = harness.addToBattlefieldAndReturn(player1, new Coretapper());
        coretapper.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Coretapper");
        harness.assertInGraveyard(player1, "Coretapper");
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Coretapper in response to its tap ability produces three counters")
    void tapAbilityResolvesAfterSourceIsSacrificed() {
        addCreatureReady(player1, new Coretapper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.activateAbility(player1, 0, 1, null, artifact.getId());

        harness.assertInGraveyard(player1, "Coretapper");
        harness.passBothPriorities();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Coretapper can put a charge counter on itself")
    void tapAbilityCanTargetItself() {
        Permanent coretapper = addCreatureReady(player1, new Coretapper());

        harness.activateAbility(player1, 0, 0, null, coretapper.getId());
        harness.passBothPriorities();

        assertThat(coretapper.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Coretapper targeting itself leaves no legal target")
    void sacrificeAbilityCanTargetItselfButDoesNotPutCountersInGraveyard() {
        Permanent coretapper = addCreatureReady(player1, new Coretapper());

        harness.activateAbility(player1, 0, 1, null, coretapper.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Coretapper");
        harness.assertInGraveyard(player1, "Coretapper");
        assertThat(coretapper.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("An invalid sacrifice target is rejected before Coretapper is sacrificed")
    void sacrificeAbilityCannotTargetNonArtifactPermanent() {
        addCreatureReady(player1, new Coretapper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrazedGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");

        harness.assertOnBattlefield(player1, "Coretapper");
        harness.assertNotInGraveyard(player1, "Coretapper");
    }
}
