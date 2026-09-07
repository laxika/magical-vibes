package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VadmirNewBlood.class, Shock.class})
class VadmirNewBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller commits a crime")
    void putsCounterOnCrime() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());

        castShockAtOpponent();

        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, vadmir)).isEqualTo(3);
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Has menace and lifelink with four or more +1/+1 counters")
    void gainsKeywordsAtFourCounters() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());

        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.LIFELINK)).isFalse();

        vadmir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.LIFELINK)).isTrue();
    }

    private void castShockAtOpponent() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
