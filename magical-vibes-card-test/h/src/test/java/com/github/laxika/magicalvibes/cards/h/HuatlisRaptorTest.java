package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuatlisRaptor.class})
class HuatlisRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("When Huatli's Raptor enters, it proliferates")
    void proliferatesWhenItEnters() {
        Permanent raptor = addCreatureReady(player1, new HuatlisRaptor());
        raptor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new HuatlisRaptor(), "{G}{W}");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(raptor.getId()));

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void proliferatesAllCounterKindsOnChosenPermanentsAndPlayers() {
        Permanent own = addCreatureReady(player1, new HuatlisRaptor());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        own.setCounterCount(CounterType.CHARGE, 3);
        Permanent opposing = addCreatureReady(player2, new HuatlisRaptor());
        opposing.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent unchosen = addCreatureReady(player2, new HuatlisRaptor());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        gd.playerExperienceCounters.put(player2.getId(), 4);
        gd.playerRadCounters.put(player2.getId(), 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        harness.castFromHand(player1, new HuatlisRaptor(), "{G}{W}");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(own.getId(), opposing.getId(), player2.getId()));

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(own.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(opposing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void mayChooseNothingEvenWhenCountersExist() {
        Permanent permanent = addCreatureReady(player1, new HuatlisRaptor());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        harness.castFromHand(player1, new HuatlisRaptor(), "{G}{W}");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithoutAChoiceWhenNobodyHasCounters() {
        harness.castFromHand(player1, new HuatlisRaptor(), "{G}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Huatli's Raptor");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Huatli's Raptor").getCounters()).isEmpty();
    }

    @Test
    void entryTriggerStillProliferatesAfterItsSourceLeaves() {
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.castFromHand(player1, new HuatlisRaptor(), "{G}{W}");
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Huatli's Raptor");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Huatli's Raptor");
    }

    @Test
    void vigilanceKeepsAttackingRaptorUntapped() {
        Permanent raptor = addCreatureReady(player1, new HuatlisRaptor());
        addCreatureReady(player2, new HuatlisRaptor());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(raptor.isTapped()).isFalse();
        assertThat(raptor.isAttacking()).isTrue();
    }
}
