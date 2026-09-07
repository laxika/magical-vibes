package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringOverseer.class, GrizzlyBears.class})
class InspiringOverseerTest extends BaseCardTest {

    @Test
    void entersAndItsTriggerGainsLifeAndDrawsACard() {
        harness.setHand(player1, List.of(new InspiringOverseer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(GrizzlyBears.class);
    }
}
