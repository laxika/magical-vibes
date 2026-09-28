package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TruthOrConsequences.class, Forest.class})
class TruthOrConsequencesTest extends BaseCardTest {

    @Test
    @DisplayName("Truth votes draw one card per truth vote")
    void truthVotesDrawCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        cast();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Consequences votes deal three damage per consequences vote to a random opponent")
    void consequencesVotesDealDamage() {
        cast();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Mixed votes draw for truth and deal damage for consequences")
    void mixedVotesApplyBothResults() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        cast();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 17);
    }

    private void cast() {
        harness.setHand(player1, List.of(new TruthOrConsequences()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
