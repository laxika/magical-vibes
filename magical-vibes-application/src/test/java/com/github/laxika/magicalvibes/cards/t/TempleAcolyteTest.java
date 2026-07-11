package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TempleAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when it enters the battlefield")
    void gainsThreeLifeOnEnter() {
        harness.setHand(player1, List.of(new TempleAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve GainLifeEffect

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Started at 20, gained 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }
}
