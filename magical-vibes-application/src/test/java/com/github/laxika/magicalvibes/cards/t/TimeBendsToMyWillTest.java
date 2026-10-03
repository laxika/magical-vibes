package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeBendsToMyWill.class, SafeholdSentry.class})
class TimeBendsToMyWillTest extends BaseCardTest {

    @Test
    @DisplayName("Setting the scheme in motion queues an extra turn")
    void queuesExtraTurn() {
        resolveScheme();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("The extra turn skips its untap step")
    void skipsUntapStepOnExtraTurn() {
        Permanent sentry = addCreatureReady(player1, new SafeholdSentry());
        sentry.tap();
        resolveScheme();

        advanceTurn(player1);

        assertThat(sentry.isTapped()).isTrue();
    }

    private void resolveScheme() {
        TimeBendsToMyWill scheme = new TimeBendsToMyWill();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }

    private void advanceTurn(Player activePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(activePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
