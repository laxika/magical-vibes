package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlazingBladeAskari.class, GrizzlyBears.class})
class BlazingBladeAskariTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {2} makes it colorless until end of turn")
    void activatingMakesItColorless() {
        Permanent askari = harness.addToBattlefieldAndReturn(player1, new BlazingBladeAskari());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.getEffectiveColors(gd, askari)).containsExactly(CardColor.RED);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, askari)).isEmpty();
    }

    @Test
    @DisplayName("The colorless setting wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent askari = harness.addToBattlefieldAndReturn(player1, new BlazingBladeAskari());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, askari)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, askari)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot activate without paying the {2} cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new BlazingBladeAskari());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void blockerWithoutFlankingGetsMinusOneMinusOne() {
        Permanent askari = addCreatureReady(player1, new BlazingBladeAskari());
        askari.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }
}
