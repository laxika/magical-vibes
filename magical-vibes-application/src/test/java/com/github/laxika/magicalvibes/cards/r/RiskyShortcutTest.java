package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class RiskyShortcutTest extends BaseCardTest {

    @Test
    @DisplayName("Controller draws two cards and each player loses 2 life")
    void drawsTwoCardsAndEachPlayerLosesTwoLife() {
        harness.setHand(player1, List.of(new RiskyShortcut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        org.assertj.core.api.Assertions.assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(player1HandBefore + 1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, player2LifeBefore - 2);
        harness.assertInGraveyard(player1, "Risky Shortcut");
    }
}
