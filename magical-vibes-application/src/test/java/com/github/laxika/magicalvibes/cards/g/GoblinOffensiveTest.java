package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinOffensiveTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X 1/1 red Goblin tokens")
    void createsXGoblins() {
        cast(player1, 2);

        List<Permanent> goblins = goblinsOf(player1);
        assertThat(goblins).hasSize(2);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().isToken()).isTrue();
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
            assertThat(goblin.getEffectivePower()).isEqualTo(1);
            assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("With X=0, creates no tokens")
    void xZeroCreatesNoTokens() {
        cast(player1, 0);

        assertThat(goblinsOf(player1)).isEmpty();
    }

    private void cast(Player player, int xValue) {
        harness.setHand(player, List.of(new GoblinOffensive()));
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, xValue + 1);

        harness.castSorcery(player, 0, xValue);
        harness.passBothPriorities();
    }

    private List<Permanent> goblinsOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Goblin".equals(permanent.getCard().getName()))
                .toList();
    }
}
