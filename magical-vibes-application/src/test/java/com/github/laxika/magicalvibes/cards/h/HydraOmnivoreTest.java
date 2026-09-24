package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HydraOmnivore.class)
class HydraOmnivoreTest extends BaseCardTest {

    @Test
    @DisplayName("In a two-player game, combat damage is not dealt to the same opponent again")
    void doesNotDamageTheDamagedOpponentAgain() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HydraOmnivore());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }
}
