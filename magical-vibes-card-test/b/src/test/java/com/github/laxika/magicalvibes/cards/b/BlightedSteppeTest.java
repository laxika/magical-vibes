package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightedSteppe.class, GrizzlyBears.class})
class BlightedSteppeTest extends BaseCardTest {

    @Test
    @DisplayName("Blighted Steppe taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new BlightedSteppe());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays four mana, sacrifices itself, and gains two life for each creature controlled")
    void sacrificesItselfAndGainsLifeForControlledCreatures() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new BlightedSteppe());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Blighted Steppe");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
