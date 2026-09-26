package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReverseTheSands.class, LeylineOfPunishment.class, AjanisPridemate.class})
class ReverseTheSandsTest extends BaseCardTest {

    @Test
    @DisplayName("redistributes whole life totals when the controller chooses a swap")
    void redistributesWholeLifeTotals() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        castReverseTheSands();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Alice: 20; Bob: 5");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }

    @Test
    @DisplayName("offers only whole current life totals, not a split total")
    void offersOnlyWholeLifeTotals() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        castReverseTheSands();

        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("No change", "Alice: 20; Bob: 5");
        harness.handleListChoice(player1, "Alice: 20; Bob: 5");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }

    @Test
    @DisplayName("allows the controller to choose no redistribution")
    void allowsNoChange() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        castReverseTheSands();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "No change");

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("treats an assigned higher life total as life gain")
    void assignedHigherLifeTotalTriggersLifeGainAbilities() {
        addCreatureReady(player1, new AjanisPridemate());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        castReverseTheSands();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Alice: 20; Bob: 5");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ajani's Pridemate")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("does not offer a redistribution that requires a player to gain life when life gain is prohibited")
    void omitsImpossibleLifeGain() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        castReverseTheSands();

        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("No change");
        harness.handleListChoice(player1, "No change");

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    private void castReverseTheSands() {
        harness.castFromHand(player1, new ReverseTheSands(), "{6}{W}{W}");
    }
}
