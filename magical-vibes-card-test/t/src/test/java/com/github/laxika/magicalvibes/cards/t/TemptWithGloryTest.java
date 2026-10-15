package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptWithGlory.class, GrizzlyBears.class, Plains.class, VorinclexMonstrousRaider.class})
class TemptWithGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on the controller's creatures and rewards an accepting opponent")
    void acceptingOpponentGetsCountersAndRewardsController() {
        Permanent ownFirst = addCreatureReady(player1);
        Permanent ownSecond = addCreatureReady(player1);
        Permanent opponentFirst = addCreatureReady(player2);
        Permanent opponentSecond = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(ownFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant the extra counters")
    void decliningOpponentDoesNotGetExtraCounters() {
        Permanent own = addCreatureReady(player1);
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent with no creatures may accept and still reward the controller")
    void emptyOpponentBattlefieldCanAccept() {
        Permanent own = addCreatureReady(player1);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent can receive counters when the controller has no creatures")
    void emptyControllerBattlefieldDoesNotPreventOffer() {
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncreature permanents do not receive counters")
    void noncreaturesAreExcluded() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent own = addCreatureReady(player1);
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(ownLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting opponents place their own counters for Vorinclex's replacement effects")
    void acceptingOpponentPlacesTheirOwnCounters() {
        Permanent own = addCreatureReady(player1);
        Permanent vorinclex = addCreatureReady(player2, new VorinclexMonstrousRaider());
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vorinclex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void castTemptWithGlory() {
        harness.castFromHand(player1, new TemptWithGlory(), "{5}{W}");
        harness.passBothPriorities();
    }
}
