package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyriderElf.class})
class SkyriderElfTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each distinct color spent, including generic mana")
    void convergeCountsColorsSpentForGenericMana() {
        castWithMana(4, ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED, ManaColor.COLORLESS,
                ManaColor.COLORLESS, ManaColor.COLORLESS);

        Permanent elf = findPermanent(player1, "Skyrider Elf");
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Converge count is independent of the value of X")
    void convergeDoesNotUsePaidXValue() {
        castWithMana(0, ManaColor.GREEN, ManaColor.BLUE);

        Permanent elf = findPermanent(player1, "Skyrider Elf");
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castWithMana(int x, ManaColor... manaColors) {
        harness.setHand(player1, List.of(new SkyriderElf()));
        for (ManaColor manaColor : manaColors) {
            harness.addMana(player1, manaColor, 1);
        }

        gs.playCard(gd, player1, 0, x, null, null);
        harness.passBothPriorities();
    }
}
