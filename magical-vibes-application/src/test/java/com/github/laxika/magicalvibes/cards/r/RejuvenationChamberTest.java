package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RejuvenationChamber.class)
class RejuvenationChamberTest extends BaseCardTest {

    @Test
    @DisplayName("Rejuvenation Chamber enters with two fade counters")
    void entersWithFadeCounters() {
        harness.castFromHand(player1, new RejuvenationChamber(), "{3}");
        harness.passBothPriorities();

        Permanent chamber = findPermanent(player1, "Rejuvenation Chamber");
        assertThat(chamber.getCounterCount(CounterType.FADE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent chamber = harness.addToBattlefieldAndReturn(player1, new RejuvenationChamber());
        chamber.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(chamber.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Rejuvenation Chamber");
    }

    @Test
    @DisplayName("Fading leaves Rejuvenation Chamber on the battlefield after removing its last fade counter")
    void remainsAfterRemovingLastFadeCounter() {
        Permanent chamber = harness.addToBattlefieldAndReturn(player1, new RejuvenationChamber());
        chamber.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(chamber.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Rejuvenation Chamber");
    }

    @Test
    @DisplayName("Fading does not trigger during an opponent's upkeep")
    void doesNotRemoveFadeCounterDuringOpponentsUpkeep() {
        Permanent chamber = harness.addToBattlefieldAndReturn(player1, new RejuvenationChamber());
        chamber.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(chamber.getCounterCount(CounterType.FADE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Rejuvenation Chamber");
    }

    @Test
    @DisplayName("Fading sacrifices Rejuvenation Chamber when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        harness.addToBattlefieldAndReturn(player1, new RejuvenationChamber());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rejuvenation Chamber");
    }

    @Test
    @DisplayName("Tapping Rejuvenation Chamber gains two life")
    void tappingGainsTwoLife() {
        Permanent chamber = harness.addToBattlefieldAndReturn(player1, new RejuvenationChamber());
        chamber.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(chamber.isTapped()).isTrue();
    }
}
