package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquadronCarrier.class, StarfighterPilot.class, GrizzlyBears.class})
class SquadronCarrierTest extends BaseCardTest {

    @Test
    @DisplayName("A Spacecraft can exhaust to conjure a nontoken Starfighter Pilot")
    void exhaustConjuresStarfighterPilot() {
        harness.addToBattlefield(player1, new SquadronCarrier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent pilot = findPermanent(player1, "Starfighter Pilot");
        assertThat(pilot.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("The exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        harness.addToBattlefield(player1, new SquadronCarrier());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Ten charge counters give your creatures flying")
    void tenChargeCountersGrantFlyingToYourCreatures() {
        Permanent carrier = harness.addToBattlefieldAndReturn(player1, new SquadronCarrier());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        carrier.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        carrier.setCounterCount(CounterType.CHARGE, 10);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The carrier cannot use a second exhaust ability from its own grant")
    void ownGrantDoesNotProvideAnExtraExhaustActivation() {
        harness.addToBattlefield(player1, new SquadronCarrier());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Starfighter Pilot")).isEqualTo(1);
    }

    @Test
    @DisplayName("Station taps a creature and adds charge counters equal to its power")
    void stationAddsChargeCounters() {
        Permanent carrier = harness.addToBattlefieldAndReturn(player1, new SquadronCarrier());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(carrier.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ten charge counters make the carrier a creature until it drops below ten")
    void tenChargeCountersAnimateCarrier() {
        Permanent carrier = harness.addToBattlefieldAndReturn(player1, new SquadronCarrier());

        carrier.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, carrier)).isFalse();

        carrier.setCounterCount(CounterType.CHARGE, 10);
        assertThat(gqs.isCreature(gd, carrier)).isTrue();

        carrier.setCounterCount(CounterType.CHARGE, 11);
        assertThat(gqs.isCreature(gd, carrier)).isTrue();

        carrier.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, carrier)).isFalse();
    }

    @Test
    @DisplayName("The carrier's flying grant includes itself when it is a creature")
    void animatedCarrierReceivesItsOwnFlyingGrant() {
        Permanent carrier = harness.addToBattlefieldAndReturn(player1, new SquadronCarrier());
        carrier.setAnimatedUntilEndOfTurn(true);
        carrier.setAnimatedPower(4);
        carrier.setAnimatedToughness(4);

        carrier.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.hasKeyword(gd, carrier, Keyword.FLYING)).isFalse();

        carrier.setCounterCount(CounterType.CHARGE, 10);
        assertThat(gqs.hasKeyword(gd, carrier, Keyword.FLYING)).isTrue();

        carrier.setCounterCount(CounterType.CHARGE, 11);
        assertThat(gqs.hasKeyword(gd, carrier, Keyword.FLYING)).isTrue();

        carrier.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.hasKeyword(gd, carrier, Keyword.FLYING)).isFalse();
    }
}
