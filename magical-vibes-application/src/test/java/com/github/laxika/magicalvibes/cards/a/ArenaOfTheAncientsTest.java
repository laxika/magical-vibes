package com.github.laxika.magicalvibes.cards.a;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArenaOfTheAncientsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield taps all legendary creatures")
    void entersAndTapsLegendaryCreatures() {
        Permanent ownLegendary = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent opponentLegendary = harness.addToBattlefieldAndReturn(player2, new ArvadTheCursed());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArenaOfTheAncients()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownLegendary.isTapped()).isTrue();
        assertThat(opponentLegendary.isTapped()).isTrue();
        assertThat(nonlegendary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Legendary creatures remain tapped during their controllers' untap steps")
    void legendaryCreaturesDoNotUntap() {
        harness.addToBattlefield(player1, new ArenaOfTheAncients());
        Permanent opponentLegendary = harness.addToBattlefieldAndReturn(player2, new ArvadTheCursed());
        Permanent opponentNonlegendary = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentLegendary.tap();
        opponentNonlegendary.tap();

        advanceToNextTurn(player1);

        assertThat(opponentLegendary.isTapped()).isTrue();
        assertThat(opponentNonlegendary.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
