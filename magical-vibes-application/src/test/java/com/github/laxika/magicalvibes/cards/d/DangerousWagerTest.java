package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DangerousWager.class, AbundantGrowth.class, PillarOfFlame.class, Forest.class, Mountain.class})
class DangerousWagerTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the rest of the hand, then draws two cards")
    void discardsHandThenDrawsTwo() {
        harness.setHand(player1, List.of(new DangerousWager(), new AbundantGrowth(), new PillarOfFlame()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Forest", "Mountain");
        harness.assertInGraveyard(player1, "Abundant Growth");
        harness.assertInGraveyard(player1, "Pillar of Flame");
        harness.assertInGraveyard(player1, "Dangerous Wager");
    }

    @Test
    @DisplayName("Still draws two cards with an otherwise empty hand")
    void emptyHandStillDrawsTwo() {
        harness.setHand(player1, List.of(new DangerousWager()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Only the caster discards and draws")
    void opponentUnaffected() {
        harness.setHand(player2, List.of(new AbundantGrowth(), new PillarOfFlame()));
        harness.setHand(player1, List.of(new DangerousWager()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Discard happens on resolution, not when paying to cast")
    void discardsCurrentHandOnlyOnResolution() {
        harness.setHand(player1, List.of(new DangerousWager(), new AbundantGrowth()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);

        harness.assertInHand(player1, "Abundant Growth");
        harness.assertNotInGraveyard(player1, "Abundant Growth");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.setHand(player1, List.of(new AbundantGrowth(), new PillarOfFlame()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abundant Growth");
        harness.assertInGraveyard(player1, "Pillar of Flame");
        harness.assertInGraveyard(player1, "Dangerous Wager");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest", "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
