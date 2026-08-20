package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Amnesia.class, Forest.class, GrizzlyBears.class, Peek.class})
class AmnesiaTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the target player's hand and discards all nonland cards")
    void discardsAllNonlandCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new Amnesia()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(card -> card.getName().equals("Forest"));
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    @DisplayName("Leaves lands in the target player's hand")
    void leavesLandsInHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, List.of(new Amnesia()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
