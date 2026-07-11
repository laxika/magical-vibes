package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringOfEternalPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Spring of Eternal Peace gains 8 life for its controller")
    void gains8Life() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SpringOfEternalPeace()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }
}
