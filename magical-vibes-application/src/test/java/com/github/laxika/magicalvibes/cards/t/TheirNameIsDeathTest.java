package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({TheirNameIsDeath.class, GrizzlyBears.class, IronMyr.class, HowlingMine.class})
class TheirNameIsDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonartifact creatures and leaves artifact creatures and other permanents untouched")
    void destroysNonartifactCreaturesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addToBattlefield(player1, new HowlingMine());

        harness.setHand(player1, List.of(new TheirNameIsDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Iron Myr");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player1, "Their Name Is Death");
    }
}
