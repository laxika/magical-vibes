package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlitherheadTest extends BaseCardTest {

    private void readyScavenge() {
        harness.setGraveyard(player1, List.of(new Slitherhead()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Scavenge puts +1/+1 counters equal to Slitherhead's power (1) on target creature")
    void scavengePutsCountersEqualToPower() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Scavenge exiles Slitherhead as a cost, so it leaves the graveyard")
    void scavengeExilesTheCard() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Slitherhead");
    }

    @Test
    @DisplayName("Scavenge can target an opponent's creature")
    void scavengeCanTargetOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scavenge requires a creature target")
    void scavengeRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyScavenge();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scavenge can only be activated as a sorcery")
    void scavengeIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Slitherhead()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
