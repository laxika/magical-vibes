package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TrainedPronghorn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeepWatch.class, SuntailHawk.class, TrainedPronghorn.class})
class KeepWatchTest extends BaseCardTest {

    private void markAttacking(Player player, String cardName) {
        Permanent permanent = findPermanent(player, cardName);
        permanent.setAttacking(true);
    }

    @Test
    @DisplayName("Draws one card for each attacking creature across all players")
    void drawsForEachAttackingCreature() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new TrainedPronghorn());
        markAttacking(player1, "Suntail Hawk");
        markAttacking(player2, "Suntail Hawk");
        markAttacking(player2, "Trained Pronghorn");

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFromHand(player1, new KeepWatch(), "{2}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Draws no cards when there are no attacking creatures")
    void drawsNoCardsWithoutAttackers() {
        harness.addToBattlefield(player1, new TrainedPronghorn());

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFromHand(player1, new KeepWatch(), "{2}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }
}
