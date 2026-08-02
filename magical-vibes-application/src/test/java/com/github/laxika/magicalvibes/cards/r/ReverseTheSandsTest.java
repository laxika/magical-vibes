package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new ReverseTheSands()));
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.castSorcery(player1, 0, 0);
    }
}
