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
    @DisplayName("A Spacecraft can exhaust to conjure a Starfighter Pilot token")
    void exhaustConjuresStarfighterPilot() {
        harness.addToBattlefield(player1, new SquadronCarrier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent pilot = findPermanent(player1, "Starfighter Pilot");
        assertThat(pilot.getCard().isToken()).isTrue();
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
}
