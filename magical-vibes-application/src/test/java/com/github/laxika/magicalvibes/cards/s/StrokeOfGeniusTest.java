package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrokeOfGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws X cards")
    void targetPlayerDrawsXCards() {
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 6); // X=3: {3}{2}{U} = 6
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("With X=0, target player draws no cards")
    void xZeroDrawsNoCards() {
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;

        harness.castInstant(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
