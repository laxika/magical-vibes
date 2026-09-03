package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.d.DarkSphere;
import com.github.laxika.magicalvibes.cards.m.MazeOfIth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Amnesia.class, BogRats.class, DarkSphere.class, MazeOfIth.class})
class AmnesiaTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the target player's hand and discards all nonland cards")
    void discardsAllNonlandCards() {
        harness.setHand(player2, List.of(new MazeOfIth(), new BogRats(), new DarkSphere()));
        harness.setHand(player1, List.of(new Amnesia()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bog Rats");
        harness.assertInGraveyard(player2, "Dark Sphere");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(card -> card.getName().equals("Maze of Ith"));
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    @DisplayName("Leaves lands in the target player's hand")
    void leavesLandsInHand() {
        harness.setHand(player2, List.of(new MazeOfIth(), new MazeOfIth()));
        harness.setHand(player1, List.of(new Amnesia()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target itself and discard its own nonland cards")
    void canTargetItself() {
        harness.setHand(player1, List.of(new MazeOfIth(), new DarkSphere(), new Amnesia()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 2, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark Sphere");
        harness.assertInHand(player1, "Maze of Ith");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
