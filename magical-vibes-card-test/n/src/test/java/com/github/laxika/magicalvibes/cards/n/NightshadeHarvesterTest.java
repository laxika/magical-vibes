package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightshadeHarvester.class, Forest.class, WitchbaneOrb.class})
class NightshadeHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's land entering makes them lose 1 life and puts a +1/+1 counter on Nightshade Harvester")
    void opponentLandEnteringTriggers() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new NightshadeHarvester());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A land entering under its controller's control does not trigger")
    void controllerLandEnteringDoesNotTrigger() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new NightshadeHarvester());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A land played by an opponent also triggers")
    void opponentPlayedLandTriggers() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new NightshadeHarvester());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    @Test
    @DisplayName("Each opponent land entry causes a separate life loss and counter")
    void multipleLandEntriesTriggerIndependently() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new NightshadeHarvester());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent still loses life when Nightshade Harvester dies before resolution")
    void triggerResolvesAfterHarvesterDies() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new NightshadeHarvester());
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player2, new Forest());

        harvester.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Nightshade Harvester");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Nightshade Harvester");
    }

    @Test
    @DisplayName("Opponent hexproof does not stop the non-targeting land trigger")
    void opponentHexproofDoesNotStopTrigger() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new NightshadeHarvester());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
