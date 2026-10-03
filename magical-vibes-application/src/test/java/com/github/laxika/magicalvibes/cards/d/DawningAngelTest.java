package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawningAngel.class, Murder.class})
class DawningAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 4 life")
    void etbGainsFourLife() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DawningAngel(), "{4}{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Entering without being cast gains life only for its controller")
    void opponentEntryGainsLifeOnlyForOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        harness.enterBattlefieldAndReturn(player2, new DawningAngel());
        harness.assertLife(player2, 10);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Life gain trigger resolves after Dawning Angel is destroyed")
    void gainsLifeAfterSourceIsDestroyed() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent angel = harness.enterBattlefieldAndReturn(player1, new DawningAngel());
        harness.assertLife(player1, 20);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dawning Angel");
        harness.assertLife(player1, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
