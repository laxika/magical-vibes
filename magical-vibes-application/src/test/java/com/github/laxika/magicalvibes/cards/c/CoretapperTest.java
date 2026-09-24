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
}
