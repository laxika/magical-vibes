package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StormCaller.class)
class StormCallerTest extends BaseCardTest {

    @Test
    @DisplayName("When Storm Caller enters, it deals 2 damage to each opponent")
    void dealsDamageToEachOpponentOnEntry() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 13);
        harness.setHand(player1, List.of(new StormCaller()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(11);
    }
}
