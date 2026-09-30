package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MagmaticScorchwing.class, EvolvingWilds.class, Forest.class, GrizzlyBears.class})
class MagmaticScorchwingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage when the library has no nonbasic land cards")
    void dealsDamageWithNoNonbasicLandsInLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        castScorchwing();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when the library contains a nonbasic land card")
    void doesNotTriggerWithNonbasicLandInLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new EvolvingWilds()));
        castScorchwing();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castScorchwing() {
        harness.setHand(player1, List.of(new MagmaticScorchwing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
