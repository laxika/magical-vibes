package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Harmonize.class, HealingLeaves.class})
class HarmonizeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Harmonize draws three cards")
    void castingDrawsThreeCards() {
        HealingLeaves first = new HealingLeaves();
        HealingLeaves second = new HealingLeaves();
        HealingLeaves third = new HealingLeaves();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new Harmonize()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.stack).isEmpty();
    }
}
