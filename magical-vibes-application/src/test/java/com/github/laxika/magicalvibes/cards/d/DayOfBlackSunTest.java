package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DayOfBlackSun.class, DarksteelMyr.class, GrizzlyBears.class, HillGiant.class})
class DayOfBlackSunTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with mana value X or less and spares larger creatures")
    void destroysCreaturesWithinManaValueBound() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castDayOfBlackSun(2);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Removes indestructible before destroying affected creatures")
    void removesAbilitiesBeforeDestruction() {
        harness.addToBattlefield(player2, new DarksteelMyr());

        castDayOfBlackSun(3);

        harness.assertInGraveyard(player2, "Darksteel Myr");
    }

    @Test
    @DisplayName("X equals zero only affects zero-mana-value creatures")
    void xZeroSpareNonzeroManaValueCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDayOfBlackSun(0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castDayOfBlackSun(int xValue) {
        harness.setHand(player1, List.of(new DayOfBlackSun()));
        harness.addMana(player1, ManaColor.BLACK, xValue + 2);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
