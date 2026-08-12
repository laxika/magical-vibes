package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoretapperTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Coretapper puts a charge counter on target artifact")
    void tappingPutsChargeCounterOnTargetArtifact() {
        Permanent coretapper = addReadyCoretapper(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(coretapper.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Coretapper puts two charge counters on target artifact")
    void sacrificingPutsTwoChargeCountersOnTargetArtifact() {
        addReadyCoretapper(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Coretapper");
        harness.assertInGraveyard(player1, "Coretapper");
    }

    @Test
    @DisplayName("Coretapper cannot target a non-artifact permanent")
    void cannotTargetNonArtifactPermanent() {
        addReadyCoretapper(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    private Permanent addReadyCoretapper(Player player) {
        Permanent permanent = new Permanent(new Coretapper());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
