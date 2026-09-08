package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HoneyMammoth.class)
class HoneyMammothTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 4 life")
    void etbGainsFourLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new HoneyMammoth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HoneyMammoth);
    }
}
