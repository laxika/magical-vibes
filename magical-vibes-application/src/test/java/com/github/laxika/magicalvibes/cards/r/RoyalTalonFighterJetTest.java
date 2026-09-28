package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoyalTalonFighterJet.class, GrizzlyBears.class})
class RoyalTalonFighterJetTest extends BaseCardTest {

    @Test
    void entersWithXCountersAndCreatesSoldiersEqualToThatNumber() {
        harness.setHand(player1, List.of(new RoyalTalonFighterJet()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jet = findPermanent(player1, "Royal Talon Fighter Jet");
        assertThat(jet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player1, "Soldier")).hasSize(3);
    }

    @Test
    void attackCreatesSoldiersEqualToCurrentCounters() {
        Permanent jet = addReadyJet(player1, 4);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jet), null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(4);
        assertThat(jet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void crewTwoAnimatesTheJetAndTapsTheCrew() {
        Permanent jet = addReadyJet(player1, 0);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jet), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jet)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addReadyJet(Player player, int counters) {
        Permanent jet = harness.addToBattlefieldAndReturn(player, new RoyalTalonFighterJet());
        jet.setSummoningSick(false);
        jet.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return jet;
    }
}
