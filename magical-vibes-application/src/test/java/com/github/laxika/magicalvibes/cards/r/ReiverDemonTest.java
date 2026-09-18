package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.cards.n.NimLasher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ReiverDemon.class, FangrenHunter.class, AlphaMyr.class, NimLasher.class, GreatFurnace.class})
class ReiverDemonTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, destroys nonartifact, nonblack creatures and they cannot be regenerated")
    void castFromHandDestroysNonartifactNonblackCreatures() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FangrenHunter());
        hunter.setRegenerationShield(1);
        harness.addToBattlefield(player1, new GreatFurnace());
        harness.addToBattlefield(player1, new AlphaMyr());
        harness.addToBattlefield(player2, new NimLasher());
        harness.addToBattlefield(player2, new FangrenHunter());

        harness.castFromHand(player1, new ReiverDemon(), "{4}{B}{B}{B}{B}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fangren Hunter");
        harness.assertOnBattlefield(player1, "Great Furnace");
        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertOnBattlefield(player2, "Nim Lasher");
        harness.assertInGraveyard(player2, "Fangren Hunter");
        harness.assertOnBattlefield(player1, "Reiver Demon");
    }

    @Test
    @DisplayName("When it enters without being cast from hand, its ability does not destroy creatures")
    void enteringWithoutBeingCastFromHandDoesNotDestroyCreatures() {
        harness.addToBattlefield(player2, new FangrenHunter());

        harness.enterBattlefieldAndReturn(player1, new ReiverDemon());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Reiver Demon");
        harness.assertOnBattlefield(player2, "Fangren Hunter");
    }
}
