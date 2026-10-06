package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TrainedPronghorn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeepWatch.class, SuntailHawk.class, TrainedPronghorn.class})
class KeepWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each attacking creature across all players")
    void drawsForEachAttackingCreature() {
        harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new SuntailHawk()).setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new TrainedPronghorn()).setAttacking(true);

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

    @Test
    @DisplayName("Counts attackers at resolution after one leaves the battlefield")
    void countsAttackersAtResolution() {
        Permanent removedAttacker = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        removedAttacker.setAttacking(true);
        harness.addToBattlefieldAndReturn(player1, new TrainedPronghorn()).setAttacking(true);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFromHand(player1, new KeepWatch(), "{2}{U}");
        gd.playerBattlefields.get(player1.getId()).remove(removedAttacker);
        gd.playerGraveyards.get(player1.getId()).add(removedAttacker.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws for opposing attackers including blocked and untapped attackers, but not blockers or other tapped creatures")
    void drawsOnlyForAttackingCreatures() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new TrainedPronghorn());
        attacker.setAttacking(true);
        attacker.untap();
        attacker.setBlockedThisCombat(true);
        harness.addToBattlefieldAndReturn(player1, new TrainedPronghorn()).setBlocking(true);
        harness.addToBattlefieldAndReturn(player2, new SuntailHawk()).tap();
        harness.setHand(player2, java.util.List.of());

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentDeckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.castFromHand(player1, new KeepWatch(), "{2}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSizeBefore);
    }
}
