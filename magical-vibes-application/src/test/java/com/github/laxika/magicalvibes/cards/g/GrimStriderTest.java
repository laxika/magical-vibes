package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrimStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Is a full 6/6 when its controller's hand is empty")
    void fullSizeWithEmptyHand() {
        harness.setHand(player1, List.of());
        Permanent strider = addStrider(player1);

        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, strider)).isEqualTo(6);
    }

    @Test
    @DisplayName("Gets -1/-1 for each card in its controller's hand")
    void shrinksPerCardInHand() {
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        Permanent strider = addStrider(player1);

        // 6/6 with -2/-2 for the two cards in hand
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, strider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only counts the controller's hand, not the opponent's")
    void ignoresOpponentHand() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        Permanent strider = addStrider(player1);

        // Only player1's single card counts
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, strider)).isEqualTo(5);
    }

    @Test
    @DisplayName("Dies to state-based actions when toughness drops to 0")
    void diesWhenHandIsLarge() {
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        addStrider(player1);

        // 6 cards in hand => -6/-6 => 0/0
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grim Strider"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grim Strider"));
    }

    @Test
    @DisplayName("P/T updates dynamically as the hand changes")
    void updatesDynamically() {
        harness.setHand(player1, List.of(new Forest()));
        Permanent strider = addStrider(player1);

        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(5);

        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(3);

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(6);
    }

    // ===== Helpers =====

    private Permanent addStrider(Player player) {
        Card card = new GrimStrider();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
