package com.github.laxika.magicalvibes.cards.f;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({ForebodingFruit.class, GrizzlyBears.class, Island.class})
class ForebodingFruitTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards and loses 2 life without adamant")
    void drawsAndLosesLifeWithoutAdamant() {
        GrizzlyBears bears = new GrizzlyBears();
        Island island = new Island();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(bears, island));

        castForebodingFruit(player2.getId(), 1, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears, island);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Adamant creates a Food token after at least three black mana is spent")
    void adamantCreatesFood() {
        GrizzlyBears bears = new GrizzlyBears();
        Island island = new Island();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(bears, island));

        castForebodingFruit(player2.getId(), 3, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears, island);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ForebodingFruit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castForebodingFruit(UUID targetPlayerId, int blackMana, int colorlessMana) {
        harness.setHand(player1, List.of(new ForebodingFruit()));
        harness.addMana(player1, ManaColor.BLACK, blackMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
