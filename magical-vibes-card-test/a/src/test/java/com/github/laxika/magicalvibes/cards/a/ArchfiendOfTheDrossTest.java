package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SheoldredsEdict;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchfiendOfTheDross.class, GrizzlyBears.class, Shock.class, SheoldredsEdict.class})
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
        harness.castAndResolveInstant(player1, 0, bearsId);
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
        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("No oil counters causes a loss only when the upkeep ability resolves")
    void zeroCountersStillCausesUpkeepLoss() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfTheDross());
        archfiend.setCounterCount(CounterType.OIL, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        advanceToUpkeep(player1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent's upkeep does not remove oil counters")
    void opponentsUpkeepDoesNotRemoveCounters() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfTheDross());
        archfiend.setCounterCount(CounterType.OIL, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(archfiend.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Leaving with one oil counter in response to upkeep prevents the loss")
    void leavingWithOneCounterPreventsLoss() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfTheDross());
        archfiend.setCounterCount(CounterType.OIL, 1);
        advanceToUpkeep(player1);

        harness.setHand(player2, List.of(new SheoldredsEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castModalInstant(player2, 0, 0, List.of());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Archfiend of the Dross");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Leaving with no oil counters in response to upkeep still causes a loss")
    void leavingWithZeroCountersStillCausesLoss() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfTheDross());
        archfiend.setCounterCount(CounterType.OIL, 0);
        advanceToUpkeep(player1);

        harness.setHand(player2, List.of(new SheoldredsEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castModalInstant(player2, 0, 0, List.of());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Archfiend of the Dross");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
