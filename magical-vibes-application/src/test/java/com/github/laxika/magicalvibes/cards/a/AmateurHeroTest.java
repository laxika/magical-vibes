package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(AmateurHero.class)
class AmateurHeroTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, its controller gains 2 life")
    void enteringGivesItsControllerTwoLife() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);

        harness.enterBattlefieldAndReturn(player1, new AmateurHero());
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability gives life to the permanent's controller")
    void enteringUnderPlayerTwoGivesPlayerTwoLife() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 8);

        harness.enterBattlefieldAndReturn(player2, new AmateurHero());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 10);
    }
}
