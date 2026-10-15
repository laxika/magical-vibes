package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicBlessing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Whirlwind.class, GrizzlyBears.class, WindDrake.class, AngelicBlessing.class})
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

    @Test
    @DisplayName("Destroys a creature that gained flying from another spell")
    void destroysCreatureWithGrantedFlying() {
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AngelicBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.castFromHand(player1, new Whirlwind(), "{2}{G}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves without any flying creatures")
    void resolvesWithoutFlyingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Whirlwind(), "{2}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Whirlwind");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
