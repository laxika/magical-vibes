package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyshroudCondor.class, LotusPetal.class})
class SkyshroudCondorTest extends BaseCardTest {

    @Test
    @DisplayName("Castable after another spell was cast this turn")
    void castableAfterAnotherSpell() {
        harness.castFromHand(player1, new LotusPetal(), "{0}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new SkyshroudCondor(), "{1}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(SkyshroudCondor.class);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud Condor");
    }

    @Test
    @DisplayName("Not castable when no other spell was cast this turn")
    void notCastableWithoutAnotherSpell() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new SkyshroudCondor(), "{1}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An opponent's spell does not satisfy the cast condition")
    void opponentSpellDoesNotEnableCast() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new LotusPetal(), "{0}");
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castFromHand(player1, new SkyshroudCondor(), "{1}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Putting a permanent onto the battlefield does not count as casting a spell")
    void permanentEnteringDoesNotEnableCast() {
        harness.addToBattlefield(player1, new LotusPetal());

        assertThatThrownBy(() -> harness.castFromHand(player1, new SkyshroudCondor(), "{1}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A spell cast on a previous turn does not satisfy the cast condition")
    void previousTurnSpellDoesNotEnableCast() {
        harness.castFromHand(player1, new LotusPetal(), "{0}");
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromHand(player1, new SkyshroudCondor(), "{1}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
