package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaruliGatekeepersTest extends BaseCardTest {

    @Test
    @DisplayName("With two Gates, ETB gains 7 life")
    void twoGatesGainsSevenLife() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        int before = gd.playerLifeTotals.get(player1.getId());
        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, before + 7);
    }

    @Test
    @DisplayName("With only one Gate the trigger does not fire")
    void oneGateDoesNotTrigger() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        int before = gd.playerLifeTotals.get(player1.getId());
        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, before);
    }

    @Test
    @DisplayName("Gates controlled by an opponent do not count")
    void opponentGatesDoNotCount() {
        harness.addToBattlefield(player2, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new BorosGuildgate());
        int before = gd.playerLifeTotals.get(player1.getId());
        castGatekeepers();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, before);
        harness.assertOnBattlefield(player1, "Saruli Gatekeepers");
    }

    private void castGatekeepers() {
        harness.setHand(player1, List.of(new SaruliGatekeepers()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
    }
}
