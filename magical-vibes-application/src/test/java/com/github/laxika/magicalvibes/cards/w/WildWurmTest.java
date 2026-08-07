package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.List;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WildWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield triggers the coin flip ability")
    void entersTriggersCoinFlip() {
        harness.setHand(player1, List.of(new WildWurm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("Wild Wurm");
    }

    @Test
    @DisplayName("Resolution flips a coin and Wild Wurm ends in exactly one legal zone")
    void resolutionFlipsCoinAndMovesOrStays() {
        harness.setHand(player1, List.of(new WildWurm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        boolean onBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Wild Wurm"));
        boolean inHand = gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Wild Wurm"));

        assertThat(onBattlefield != inHand).isTrue();
        if (inHand) {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                    .anyMatch(log -> log.contains("returned to its owner's hand"));
        }

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Wild Wurm"));
    }
}
