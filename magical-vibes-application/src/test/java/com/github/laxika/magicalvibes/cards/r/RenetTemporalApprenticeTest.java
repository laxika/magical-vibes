package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.util.List.of;

@CardUsed({RenetTemporalApprentice.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class})
@DisplayName("Renet, Temporal Apprentice")
class RenetTemporalApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns other nonland permanents that entered this turn")
    void returnsOtherNonlandPermanentsThatEnteredThisTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.enterBattlefieldAndReturn(player2, new Forest());

        castRenet();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Renet, Temporal Apprentice");
    }

    private void castRenet() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, of(new RenetTemporalApprentice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
