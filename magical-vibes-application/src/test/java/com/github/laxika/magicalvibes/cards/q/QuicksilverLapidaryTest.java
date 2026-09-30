package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuicksilverLapidary.class, MoxOpal.class})
class QuicksilverLapidaryTest extends BaseCardTest {

    @Test
    void conjuresMoxOpalIntoHand() {
        harness.enterBattlefieldAndReturn(player1, new QuicksilverLapidary());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Mox Opal");
                    assertThat(card.isToken()).isFalse();
                });
    }
}
