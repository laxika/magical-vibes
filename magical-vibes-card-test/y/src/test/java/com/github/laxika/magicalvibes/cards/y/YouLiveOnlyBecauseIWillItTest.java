package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(YouLiveOnlyBecauseIWillIt.class)
class YouLiveOnlyBecauseIWillItTest extends BaseCardTest {

    @Test
    @DisplayName("may redistribute the players' current life totals")
    void redistributesLifeTotals() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        resolveScheme();
        harness.handleListChoice(player1, "Alice: 20; Bob: 5");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }

    @Test
    @DisplayName("may leave all life totals unchanged")
    void mayChooseNoChange() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        resolveScheme();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("No change");
        harness.handleListChoice(player1, "No change");

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    private void resolveScheme() {
        Card scheme = new YouLiveOnlyBecauseIWillIt();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
