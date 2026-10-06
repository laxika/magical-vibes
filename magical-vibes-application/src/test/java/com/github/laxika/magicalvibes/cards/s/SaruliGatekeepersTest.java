package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaruliGatekeepers.class, AzoriusGuildgate.class, BorosGuildgate.class})
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

    @Test
    @DisplayName("Losing a Gate before resolution prevents life gain")
    void losingGateBeforeResolutionPreventsLifeGain() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        var gate = harness.addToBattlefieldAndReturn(player1, new BorosGuildgate());
        int before = gd.playerLifeTotals.get(player1.getId());
        castGatekeepers();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gate);
        harness.passBothPriorities();

        harness.assertLife(player1, before);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two Gates with the same name satisfy the condition")
    void sameNamedGatesCountSeparately() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        int before = gd.playerLifeTotals.get(player1.getId());
        castGatekeepers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, before + 7);
    }

    @Test
    @DisplayName("The trigger still gains life after Gatekeepers leaves")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        int before = gd.playerLifeTotals.get(player1.getId());
        castGatekeepers();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        var source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SaruliGatekeepers)
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        harness.passBothPriorities();

        harness.assertLife(player1, before + 7);
    }

    private void castGatekeepers() {
        harness.castFromHand(player1, new SaruliGatekeepers(), "{3}{G}");
    }
}
