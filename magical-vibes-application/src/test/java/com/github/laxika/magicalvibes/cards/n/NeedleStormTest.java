package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeedleStorm.class, WindDrake.class, GrizzlyBears.class})
class NeedleStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each creature with flying, on both battlefields")
    void dealsFourDamageToFlyingCreatures() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.addToBattlefield(player2, new WindDrake());

        harness.castFromHand(player1, new NeedleStorm(), "{2}{G}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wind Drake");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Does not damage non-flying creatures")
    void doesNotDamageNonFlyingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new NeedleStorm(), "{2}{G}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        harness.castFromHand(player1, new NeedleStorm(), "{2}{G}");

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals exactly 4 damage to each surviving flying creature")
    void dealsExactlyFourDamageToEachSurvivingFlyingCreature() {
        WindDrake player1Card = new WindDrake();
        player1Card.setToughness(5);
        WindDrake player2Card = new WindDrake();
        player2Card.setToughness(5);
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, player1Card);
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, player2Card);

        harness.castFromHand(player1, new NeedleStorm(), "{2}{G}");

        harness.passBothPriorities();

        assertThat(player1Creature.getMarkedDamage()).isEqualTo(4);
        assertThat(player2Creature.getMarkedDamage()).isEqualTo(4);
    }
}
