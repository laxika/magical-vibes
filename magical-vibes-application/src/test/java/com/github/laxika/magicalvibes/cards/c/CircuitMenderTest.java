package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircuitMender.class, Plains.class, WrathOfGod.class})
class CircuitMenderTest extends BaseCardTest {

    @Test
    @DisplayName("Circuit Mender's enters-the-battlefield ability gains 2 life")
    void entersBattlefieldGainsLife() {
        harness.setHand(player1, List.of(new CircuitMender()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Circuit Mender's leaves-the-battlefield ability draws a card")
    void leavesBattlefieldDrawsCard() {
        harness.addToBattlefield(player1, new CircuitMender());
        harness.setLibrary(player1, List.of(new Plains()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Circuit Mender");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Plains");
    }
}
