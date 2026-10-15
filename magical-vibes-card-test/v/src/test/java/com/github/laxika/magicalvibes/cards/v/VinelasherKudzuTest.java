package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Farseek;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VinelasherKudzu.class, Forest.class, Farseek.class, Mountain.class, LastGasp.class})
class VinelasherKudzuTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a land you control enters")
    void putsCounterWhenControllerPlaysLand() {
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters")
    void doesNotTriggerForOpponentLand() {
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers when a land you control enters from a spell")
    void putsCounterWhenSpellPutsLandOntoBattlefield() {
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Farseek()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each copy gets its own counter only when its landfall trigger resolves")
    void eachCopyGetsItsOwnCounterOnResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters accumulate for a played land and a land put onto the battlefield")
    void countersAccumulateForEachLandEntering() {
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Forest(), new Farseek()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A trigger from a removed Kudzu does not put a counter on another copy")
    void removedSourceDoesNotPutCounterOnAnotherCopy() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.playLand(player1, 0);
        harness.castAndResolveInstant(player2, 0, removed.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(removed);
        resolveAllTriggers();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
