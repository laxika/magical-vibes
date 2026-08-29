package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class EnatuGolemTest extends BaseCardTest {

    @Test
    @DisplayName("When Enatu Golem dies, its controller gains 4 life")
    void gainsLifeWhenItDies() {
        harness.addToBattlefield(player1, new EnatuGolem());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }
}
