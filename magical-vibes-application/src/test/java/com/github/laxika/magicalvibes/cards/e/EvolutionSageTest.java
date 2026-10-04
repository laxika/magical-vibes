package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvolutionSage.class, Forest.class, PrimordialWurm.class})
class EvolutionSageTest extends BaseCardTest {

    @Test
    @DisplayName("A land entering under your control triggers proliferate")
    void ownLandTriggersProliferate() {
        harness.addToBattlefield(player1, new EvolutionSage());

        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        wurm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(wurm.getId()));

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land entering does not trigger proliferate")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new EvolutionSage());

        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        wurm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        harness.addToBattlefield(player1, new EvolutionSage());

        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        wurm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate adds each existing counter kind to chosen permanents and players")
    void proliferatesAllKindsOnBothSides() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new EvolutionSage());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        sage.setCounterCount(CounterType.STUN, 1);
        Permanent opponentSage = harness.addToBattlefieldAndReturn(player2, new EvolutionSage());
        opponentSage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new Forest());
        unchosen.setCounterCount(CounterType.STUN, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(sage.getId(), opponentSage.getId(), player2.getId()));

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(sage.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(opponentSage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("A land entering without being played still triggers landfall")
    void landPutOntoBattlefieldTriggers() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new EvolutionSage());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(sage.getId()));

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall resolves even when Evolution Sage leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new EvolutionSage());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(sage);
        harness.setGraveyard(player1, List.of(sage.getCard()));

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall with no counters resolves without a choice")
    void noCountersResolvesWithoutChoice() {
        harness.addToBattlefield(player1, new EvolutionSage());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getCounters()).isEmpty());
    }
}
