package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ChaplainsBlessing.class})
class ChaplainsBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 5 life")
    void gainsFiveLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ChaplainsBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Life gain happens only on resolution and can exceed the starting life total")
    void gainsLifeOnlyOnResolutionAboveTwenty() {
        harness.setLife(player1, 23);
        harness.setLife(player2, 17);
        harness.setHand(player1, List.of(new ChaplainsBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, 0);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);

        harness.passBothPriorities();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Chaplain's Blessing");
    }

    @Test
    @DisplayName("The second player gains life when they cast the spell")
    void secondPlayerGainsLife() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 12);
        harness.setLife(player2, 10);
        harness.setHand(player2, List.of(new ChaplainsBlessing()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 15);
    }
}
