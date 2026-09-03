package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BatteringCraghorn.class)
class BatteringCraghornTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new BatteringCraghorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent craghorn = findPermanent(player1, "Battering Craghorn");
        assertThat(craghorn.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int craghornIndex = gd.playerBattlefields.get(player1.getId()).indexOf(craghorn);
        harness.turnFaceUp(player1, craghornIndex);
        harness.passBothPriorities();

        assertThat(craghorn.isFaceDown()).isFalse();
    }
}
