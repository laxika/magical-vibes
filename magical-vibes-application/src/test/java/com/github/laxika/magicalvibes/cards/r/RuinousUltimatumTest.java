package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        harness.setHand(player1, List.of(new RuinousUltimatum()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Howling Mine");
        harness.assertOnBattlefield(player2, "Mountain");
    }
}
