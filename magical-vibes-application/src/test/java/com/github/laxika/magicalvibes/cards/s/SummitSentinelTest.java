package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummitSentinel.class, GrizzlyBears.class, WrathOfGod.class})
class SummitSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("When Summit Sentinel dies, its controller draws a card")
    void diesDrawsCard() {
        harness.addToBattlefield(player1, new SummitSentinel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Summit Sentinel");
        harness.assertInGraveyard(player1, "Summit Sentinel");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("A death trigger waits for resolution and draws only for the dying creature's controller")
    void opponentDeathDrawsOnlyForOpponent() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        SummitSentinel drawnCard = new SummitSentinel();
        harness.setLibrary(player2, List.of(drawnCard));
        var sentinel = harness.addToBattlefieldAndReturn(player2, new SummitSentinel());
        sentinel.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Summit Sentinel");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Summit Sentinel dying simultaneously draws one card")
    void simultaneousDeathsEachDrawOneCard() {
        harness.setHand(player1, List.of());
        SummitSentinel firstDraw = new SummitSentinel();
        SummitSentinel secondDraw = new SummitSentinel();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        var first = harness.addToBattlefieldAndReturn(player1, new SummitSentinel());
        var second = harness.addToBattlefieldAndReturn(player1, new SummitSentinel());
        first.setMarkedDamage(3);
        second.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Summit Sentinel");
    }
}
