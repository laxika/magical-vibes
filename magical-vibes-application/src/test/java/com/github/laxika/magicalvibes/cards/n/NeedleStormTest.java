package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.r.RathiDragon;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeedleStorm.class, WindDrake.class, RathiDragon.class, TrainedArmodon.class})
class NeedleStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each creature with flying, on both battlefields")
    void dealsFourDamageToFlyingCreatures() {
        harness.addToBattlefield(player1, new WindDrake());
        Permanent resilientFlyingCreature = harness.addToBattlefieldAndReturn(player2, new RathiDragon());

        harness.castFromHand(player1, new NeedleStorm(), "{2}{G}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wind Drake");
        harness.assertOnBattlefield(player2, "Rathi Dragon");
        assertThat(resilientFlyingCreature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not damage non-flying creatures")
    void doesNotDamageNonFlyingCreatures() {
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());

        harness.castFromHand(player1, new NeedleStorm(), "{2}{G}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Trained Armodon");
        assertThat(groundCreature.getMarkedDamage()).isZero();
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
        RathiDragon player1Card = new RathiDragon();
        RathiDragon player2Card = new RathiDragon();
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, player1Card);
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, player2Card);

        harness.castFromHand(player1, new NeedleStorm(), "{2}{G}");

        harness.passBothPriorities();

        assertThat(player1Creature.getMarkedDamage()).isEqualTo(4);
        assertThat(player2Creature.getMarkedDamage()).isEqualTo(4);
    }
}
