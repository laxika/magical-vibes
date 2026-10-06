package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SkirmishRhino.class)
class SkirmishRhinoTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each opponent lose 2 life and its controller gain 2 life")
    void entersBattlefieldDrainsOpponentsAndGainsLife() {
        castSkirmishRhino();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB trigger is put on the stack after the creature resolves")
    void entersBattlefieldPutsTriggerOnStack() {
        castSkirmishRhino();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB trigger still drains life after Rhino dies")
    void triggerResolvesAfterSourceDies() {
        castSkirmishRhino();
        harness.passBothPriorities();

        findPermanent(player1, "Skirmish Rhino").setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(countPermanents(player1, "Skirmish Rhino")).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Entering without being cast drains the entering creature's opponent")
    void enteringUnderOpponentControlReversesLifeRecipients() {
        harness.enterBattlefieldAndReturn(player2, new SkirmishRhino());

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(22);
    }

    private void castSkirmishRhino() {
        harness.castFromHand(player1, new SkirmishRhino(), "{W}{B}{G}");
    }
}
