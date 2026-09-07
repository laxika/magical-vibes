package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed(Tonberry.class)
class TonberryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with a stun counter")
    void entersTappedWithStunCounter() {
        Permanent tonberry = castTonberry();

        assertThat(tonberry.isTapped()).isTrue();
        assertThat(tonberry.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Has first strike and deathtouch during its controller's turn")
    void hasKeywordsDuringControllerTurn() {
        Permanent tonberry = castTonberry();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, tonberry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, tonberry, Keyword.DEATHTOUCH)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, tonberry, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, tonberry, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent castTonberry() {
        harness.setHand(player1, List.of(new Tonberry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Tonberry");
    }
}
