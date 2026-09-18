package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DwarvenScorcher;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.t.TunnelerWurm;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SwirlingSandstorm.class, DwarvenScorcher.class, SuntailHawk.class,
        GiantWarthog.class, TunnelerWurm.class})
class SwirlingSandstormTest extends BaseCardTest {

    @Test
    @DisplayName("Without threshold, Swirling Sandstorm deals no damage")
    void doesNothingBelowThreshold() {
        harness.addToBattlefield(player1, new DwarvenScorcher());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setGraveyard(player1, List.of(
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher()));
        harness.castFromHand(player1, new SwirlingSandstorm(), "{3}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dwarven Scorcher");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("With threshold, Swirling Sandstorm deals 5 damage to non-flying creatures")
    void thresholdDamagesNonFlyingCreaturesOnly() {
        harness.addToBattlefield(player1, new DwarvenScorcher());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setGraveyard(player1, List.of(
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher()));
        harness.castFromHand(player1, new SwirlingSandstorm(), "{3}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dwarven Scorcher");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("With threshold, Swirling Sandstorm deals exactly 5 damage to non-flying creatures")
    void thresholdDealsExactlyFiveToNonFlyingCreatures() {
        harness.addToBattlefield(player1, new GiantWarthog());
        harness.addToBattlefield(player2, new GiantWarthog());
        harness.addToBattlefield(player2, new TunnelerWurm());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setGraveyard(player1, List.of(
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher(), new DwarvenScorcher(), new SwirlingSandstorm()));
        harness.castFromHand(player1, new SwirlingSandstorm(), "{3}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Warthog");
        harness.assertNotOnBattlefield(player2, "Giant Warthog");
        harness.assertOnBattlefield(player2, "Tunneler Wurm");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Threshold uses only the caster's graveyard")
    void thresholdUsesCastersGraveyardOnly() {
        harness.addToBattlefield(player1, new DwarvenScorcher());
        harness.addToBattlefield(player2, new DwarvenScorcher());
        harness.setGraveyard(player1, List.of(
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher()));
        harness.setGraveyard(player2, List.of(
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher()));
        harness.castFromHand(player1, new SwirlingSandstorm(), "{3}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dwarven Scorcher");
        harness.assertOnBattlefield(player2, "Dwarven Scorcher");
    }
}
