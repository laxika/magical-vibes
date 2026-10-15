package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Owlbear.class, NeverwinterDryad.class})
class OwlbearTest extends BaseCardTest {

    @Test
    void entersAndDrawsACard() {
        Card drawnCard = new NeverwinterDryad();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, new Owlbear(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void drawWaitsForEntryTriggerToResolve() {
        Card drawnCard = new Owlbear();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, new Owlbear(), "{3}{G}{G}");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Owlbear");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDrawsForItsController() {
        Card drawnCard = new Owlbear();
        Card otherCard = new Owlbear();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(otherCard));
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new Owlbear());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Owlbear());
        Permanent blocker = addCreatureReady(player2, new NeverwinterDryad());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Neverwinter Dryad");
        harness.assertOnBattlefield(player1, "Owlbear");
    }
}
