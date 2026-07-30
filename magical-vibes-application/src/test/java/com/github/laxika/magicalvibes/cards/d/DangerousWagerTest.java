package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DangerousWagerTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the rest of the hand, then draws two cards")
    void discardsHandThenDrawsTwo() {
        harness.setHand(player1, new ArrayList<>(List.of(new DangerousWager(), new GrizzlyBears(), new Peek())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Mountain())));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Forest", "Mountain");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Peek");
        harness.assertInGraveyard(player1, "Dangerous Wager");
    }

    @Test
    @DisplayName("Still draws two cards with an otherwise empty hand")
    void emptyHandStillDrawsTwo() {
        harness.setHand(player1, new ArrayList<>(List.of(new DangerousWager())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Mountain())));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Only the caster discards and draws")
    void opponentUnaffected() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, new ArrayList<>(List.of(new DangerousWager())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Mountain())));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
