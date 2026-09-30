package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SavageStomp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingbaneVantasaur.class, SavageStomp.class, Naturalize.class})
class WingbaneVantasaurTest extends BaseCardTest {

    @Test
    void conjuresSavageStomp() {
        cast(0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Savage Stomp")
                .doesNotContain("Naturalize");
    }

    @Test
    void conjuresNaturalize() {
        cast(1);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Naturalize")
                .doesNotContain("Savage Stomp");
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new WingbaneVantasaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, modeIndex);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
