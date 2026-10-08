package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MoltensteelDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViridianBetrayers.class, MoltensteelDragon.class})
class ViridianBetrayersTest extends BaseCardTest {


    @Test
    @DisplayName("Has infect when opponent is poisoned")
    void hasInfectWhenOpponentPoisoned() {
        Permanent betrayers = harness.addToBattlefieldAndReturn(player1, new ViridianBetrayers());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Does NOT have infect when no opponent is poisoned")
    void noInfectWhenOpponentNotPoisoned() {
        Permanent betrayers = harness.addToBattlefieldAndReturn(player1, new ViridianBetrayers());

        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Checks opponent for poison, not controller")
    void checksOpponentNotController() {
        Permanent betrayers = harness.addToBattlefieldAndReturn(player1, new ViridianBetrayers());
        // Controller has poison but opponent does not
        gd.playerPoisonCounters.put(player1.getId(), 3);

        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Gains infect dynamically when opponent becomes poisoned")
    void gainsInfectDynamically() {
        Permanent betrayers = harness.addToBattlefieldAndReturn(player1, new ViridianBetrayers());

        // Initially no infect
        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isFalse();

        // Opponent gets poisoned
        gd.playerPoisonCounters.put(player2.getId(), 1);

        // Now has infect
        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isTrue();
    }


    @Test
    void losesInfectWhenOpponentIsNoLongerPoisoned() {
        Permanent betrayers = harness.addToBattlefieldAndReturn(player1, new ViridianBetrayers());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isTrue();

        gd.playerPoisonCounters.put(player2.getId(), 0);

        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isFalse();
    }

    @Test
    void dealsPoisonInsteadOfLifeLossToPoisonedOpponent() {
        Permanent betrayers = harness.addToBattlefieldAndReturn(player1, new ViridianBetrayers());
        betrayers.setSummoningSick(false);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void dealsNormalDamageToUnpoisonedOpponent() {
        Permanent betrayers = harness.addToBattlefieldAndReturn(player1, new ViridianBetrayers());
        betrayers.setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void dealsMinusOneCountersToBlockerWhenOpponentIsPoisoned() {
        Permanent betrayers = harness.addToBattlefieldAndReturn(player1, new ViridianBetrayers());
        betrayers.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new MoltensteelDragon());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
    }
}