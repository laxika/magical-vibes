package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FriendlyTeddy.class})
class FriendlyTeddyTest extends BaseCardTest {

    @Test
    void whenItDiesEachPlayerDrawsACard() {
        FriendlyTeddy player1Draw = new FriendlyTeddy();
        FriendlyTeddy player2Draw = new FriendlyTeddy();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));

        Permanent teddy = harness.addToBattlefieldAndReturn(player1, new FriendlyTeddy());
        teddy.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Friendly Teddy");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Draw);
    }

    @Test
    void opponentsTeddyAlsoMakesBothPlayersDraw() {
        FriendlyTeddy player1Draw = new FriendlyTeddy();
        FriendlyTeddy player2Draw = new FriendlyTeddy();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));

        Permanent teddy = harness.addToBattlefieldAndReturn(player2, new FriendlyTeddy());
        teddy.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Friendly Teddy");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Draw);
    }

    @Test
    void nonlethalDamageDoesNotTriggerADraw() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new FriendlyTeddy()));
        harness.setLibrary(player2, List.of(new FriendlyTeddy()));

        Permanent teddy = harness.addToBattlefieldAndReturn(player1, new FriendlyTeddy());
        teddy.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Friendly Teddy");
        harness.assertNotInGraveyard(player1, "Friendly Teddy");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
