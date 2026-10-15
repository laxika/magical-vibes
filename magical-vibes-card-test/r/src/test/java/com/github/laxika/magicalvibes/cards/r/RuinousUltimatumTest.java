package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({RuinousUltimatum.class, GrizzlyBears.class, HowlingMine.class, Mountain.class})
class RuinousUltimatumTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys opponents' nonland permanents and leaves lands and your permanents")
    void destroysOpponentsNonlandPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new Mountain());
        harness.castFromHand(player1, new RuinousUltimatum(), "{R}{R}{W}{W}{W}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Howling Mine");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Indestructible prevents destruction")
    void sparesIndestructiblePermanent() {
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.addToBattlefield(player2, new HowlingMine());

        harness.castFromHand(player1, new RuinousUltimatum(), "{R}{R}{W}{W}{W}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Howling Mine");
    }

    @Test
    @DisplayName("Hexproof does not prevent untargeted destruction")
    void destroysHexproofPermanent() {
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setCounterCount(CounterType.HEXPROOF, 1);

        harness.castFromHand(player1, new RuinousUltimatum(), "{R}{R}{W}{W}{W}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Regeneration replaces destruction")
    void allowsRegeneration() {
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setRegenerationShield(1);

        harness.castFromHand(player1, new RuinousUltimatum(), "{R}{R}{W}{W}{W}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(bear.isTapped()).isTrue();
        org.assertj.core.api.Assertions.assertThat(bear.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Resolves without any opposing permanents")
    void resolvesAgainstEmptyBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castFromHand(player1, new RuinousUltimatum(), "{R}{R}{W}{W}{W}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ruinous Ultimatum");
    }
}
