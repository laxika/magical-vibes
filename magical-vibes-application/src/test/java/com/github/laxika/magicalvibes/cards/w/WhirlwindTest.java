package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Whirlwind.class, GrizzlyBears.class, WindDrake.class})
class WhirlwindTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures with flying and spares creatures without flying")
    void destroysOnlyCreaturesWithFlying() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WindDrake());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Whirlwind(), "{2}{G}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wind Drake");
        harness.assertInGraveyard(player2, "Wind Drake");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys flying creatures controlled by both players")
    void destroysFlyingCreaturesControlledByBothPlayers() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.addToBattlefield(player2, new WindDrake());
        harness.castFromHand(player1, new Whirlwind(), "{2}{G}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wind Drake");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
        harness.assertInGraveyard(player1, "Wind Drake");
        harness.assertInGraveyard(player2, "Wind Drake");
    }
}
