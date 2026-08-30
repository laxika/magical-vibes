package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArchfiendOfTheDrossTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four oil counters")
    void entersWithFourOilCounters() {
        harness.setHand(player1, List.of(new ArchfiendOfTheDross()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent archfiend = findPermanent(player1, "Archfiend of the Dross");
        assertThat(archfiend.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep removes one oil counter while counters remain")
    void upkeepRemovesOneOilCounterWhileCountersRemain() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfTheDross());
        archfiend.setCounterCount(CounterType.OIL, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archfiend.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Upkeep loss occurs after the last oil counter is removed")
    void losesGameAfterLastOilCounterIsRemoved() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfTheDross());
        archfiend.setCounterCount(CounterType.OIL, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archfiend.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent loses two life when their creature dies")
    void opponentLosesLifeWhenTheirCreatureDies() {
        harness.addToBattlefield(player1, new ArchfiendOfTheDross());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The death trigger does not fire for a creature controlled by its controller")
    void deathTriggerDoesNotFireForOwnCreature() {
        harness.addToBattlefield(player1, new ArchfiendOfTheDross());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 20);
    }
}
