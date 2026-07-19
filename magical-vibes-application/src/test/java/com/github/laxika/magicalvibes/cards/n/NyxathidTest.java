package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class NyxathidTest extends BaseCardTest {

    @Test
    @DisplayName("Full 7/7 when the opponent's hand is empty")
    void fullSizeWithEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        Permanent nyxathid = addNyxathid(player1);

        assertThat(gqs.getEffectivePower(gd, nyxathid)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(7);
    }

    @Test
    @DisplayName("Gets -1/-1 for each card in the opponent's hand")
    void shrinksWithOpponentHandSize() {
        harness.setHand(player2, handOf(3));
        Permanent nyxathid = addNyxathid(player1);

        assertThat(gqs.getEffectivePower(gd, nyxathid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only the opponent's hand, not the controller's own")
    void ignoresControllerHand() {
        harness.setHand(player1, handOf(5));
        harness.setHand(player2, handOf(2));
        Permanent nyxathid = addNyxathid(player1);

        assertThat(gqs.getEffectivePower(gd, nyxathid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(5);
    }

    @Test
    @DisplayName("Updates dynamically as the opponent's hand changes")
    void updatesDynamically() {
        harness.setHand(player2, handOf(2));
        Permanent nyxathid = addNyxathid(player1);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(5);

        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(4);
    }

    @Test
    @DisplayName("Dies when reduced to 0 toughness by a seven-card opponent hand")
    void diesWhenReducedToZeroToughness() {
        harness.setHand(player2, handOf(7));
        addNyxathid(player1);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nyxathid"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nyxathid"));
    }

    // ===== Helpers =====

    private Permanent addNyxathid(Player player) {
        harness.addToBattlefield(player, new Nyxathid());
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nyxathid"))
                .findFirst()
                .orElseThrow();
    }

    private List<Card> handOf(int count) {
        return new ArrayList<>(IntStream.range(0, count)
                .mapToObj(i -> (Card) new GrizzlyBears())
                .toList());
    }
}
