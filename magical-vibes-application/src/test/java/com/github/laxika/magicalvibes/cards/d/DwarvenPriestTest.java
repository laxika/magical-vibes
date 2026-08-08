package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwarvenPriestTest extends BaseCardTest {

    private void castPriest() {
        harness.setHand(player1, List.of(new DwarvenPriest()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering gains 1 life for each creature you control, counting itself")
    void gainsLifePerCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        castPriest();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Opponent's creatures are not counted")
    void ignoresOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        castPriest();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }
}
