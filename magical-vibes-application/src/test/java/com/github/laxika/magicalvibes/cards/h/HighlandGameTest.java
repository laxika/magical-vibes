package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class HighlandGameTest extends BaseCardTest {

    @Test
    @DisplayName("When Highland Game dies, its controller gains 2 life")
    void gainsLifeWhenItDies() {
        harness.addToBattlefield(player2, new HighlandGame());
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID highlandGameId = harness.getPermanentId(player2, "Highland Game");
        harness.castInstant(player1, 0, highlandGameId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Highland Game");
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
    }
}
