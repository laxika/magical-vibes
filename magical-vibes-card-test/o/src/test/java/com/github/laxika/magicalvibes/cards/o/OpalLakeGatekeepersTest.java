package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalLakeGatekeepers.class, AzoriusGuildgate.class, BorosGuildgate.class})
class OpalLakeGatekeepersTest extends BaseCardTest {

    @Test
    @DisplayName("With two Gates, accepting the ETB may ability draws a card")
    void twoGatesAcceptDraw() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve ETB trigger -> may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("With two Gates, declining the may ability draws nothing")
    void twoGatesDeclineDraw() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        castGatekeepers();
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("With only one Gate the trigger does not fire")
    void oneGateDoesNotTrigger() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        castGatekeepers();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.assertOnBattlefield(player1, "Opal Lake Gatekeepers");
    }

    @Test
    @DisplayName("Gates controlled by an opponent do not count")
    void opponentGatesDoNotCount() {
        harness.addToBattlefield(player2, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new BorosGuildgate());
        castGatekeepers();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.assertOnBattlefield(player1, "Opal Lake Gatekeepers");
    }

    @Test
    @DisplayName("Losing a Gate before resolution prevents the optional draw")
    void losingGateBeforeResolutionPreventsDraw() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        castGatekeepers();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof BorosGuildgate);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Two Gates with the same name satisfy the condition")
    void sameNamedGatesAllowDraw() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        castGatekeepers();
        harness.passBothPriorities();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private void castGatekeepers() {
        harness.castFromHand(player1, new OpalLakeGatekeepers(), "{3}{U}");
    }
}
