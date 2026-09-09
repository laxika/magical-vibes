package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TempleAcolyte.class)
class TempleAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when it enters the battlefield")
    void gainsThreeLifeOnEnter() {
        harness.setHand(player1, List.of(new TempleAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve GainLifeEffect

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }
}
