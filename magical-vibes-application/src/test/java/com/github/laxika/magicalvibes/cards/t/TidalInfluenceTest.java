package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FeralThallid;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalInfluence.class, FeralThallid.class, RiverMerfolk.class})
class TidalInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one tide counter and weakens all blue creatures")
    void entersWithOneTideCounterAndWeakensBlueCreatures() {
        Permanent influence = castTidalInfluence(player1);
        Permanent ownMerfolk = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());
        Permanent opponentMerfolk = harness.addToBattlefieldAndReturn(player2, new RiverMerfolk());
        Permanent thallid = harness.addToBattlefieldAndReturn(player1, new FeralThallid());

        assertThat(influence.getCounterCount(CounterType.TIDE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownMerfolk)).isZero();
        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isZero();
        assertThat(gqs.getEffectivePower(gd, thallid)).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot be cast while any Tidal Influence is on the battlefield")
    void cannotBeCastWhileTidalInfluenceIsOnBattlefield() {
        harness.addToBattlefield(player2, new TidalInfluence());

        assertThatThrownBy(() -> harness.castFromHand(player1, new TidalInfluence(), "{2}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Two tide counters leave blue creatures unchanged")
    void twoTideCountersLeaveBlueCreaturesUnchanged() {
        Permanent influence = harness.addToBattlefieldAndReturn(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 2);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds a tide counter only during its controller's upkeep")
    void addsTideCounterOnlyDuringControllersUpkeep() {
        Permanent influence = harness.addToBattlefieldAndReturn(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(influence.getCounterCount(CounterType.TIDE)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(influence.getCounterCount(CounterType.TIDE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zero tide counters do not modify blue creatures")
    void zeroTideCountersDoNotModifyBlueCreatures() {
        Permanent influence = harness.addToBattlefieldAndReturn(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 0);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exactly three tide counters strengthen all blue creatures")
    void threeTideCountersStrengthenBlueCreatures() {
        Permanent influence = harness.addToBattlefieldAndReturn(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 3);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(4);
    }

    @Test
    @DisplayName("Four tide counters are removed by the state-triggered ability")
    void fourTideCountersAreRemoved() {
        Permanent influence = harness.addToBattlefieldAndReturn(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(influence.getCounterCount(CounterType.TIDE)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(influence.getCounterCount(CounterType.TIDE)).isZero();
    }

    @Test
    @DisplayName("Five tide counters are also removed by the state-triggered ability")
    void fiveTideCountersAreRemoved() {
        Permanent influence = harness.addToBattlefieldAndReturn(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 5);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(influence.getCounterCount(CounterType.TIDE)).isZero();
    }

    private Permanent castTidalInfluence(Player player) {
        harness.castFromHand(player, new TidalInfluence(), "{2}{U}");
        harness.passBothPriorities();
        return findPermanent(player, "Tidal Influence");
    }
}
