package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeepWatch.class, SuntailHawk.class})
class KeepWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for attacking creatures controlled by an opponent")
    void drawsForEachAttackingCreature() {
        addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player2, new SuntailHawk());
        declareAttackers(List.of(0, 1, 2));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.castFromHand(player2, new KeepWatch(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Draws no cards when there are no attacking creatures")
    void drawsNoCardsWithoutAttackers() {
        harness.addToBattlefield(player1, new SuntailHawk());

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFromHand(player1, new KeepWatch(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }
}
