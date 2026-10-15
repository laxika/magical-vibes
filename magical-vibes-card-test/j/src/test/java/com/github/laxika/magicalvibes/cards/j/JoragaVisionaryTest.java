package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoragaVisionary.class, Forest.class})
class JoragaVisionaryTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldAndDrawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new JoragaVisionary(), "{3}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void enteringWithoutBeingCastDrawsExactlyOneCardForItsController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player2, new JoragaVisionary());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entryTriggerStillDrawsAfterVisionaryLeavesTheBattlefield() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        var visionary = harness.enterBattlefieldAndReturn(player1, new JoragaVisionary());

        gd.playerBattlefields.get(player1.getId()).remove(visionary);
        harness.setGraveyard(player1, List.of(visionary.getCard()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
