package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoidwingHybridTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard after proliferating without choosing a permanent")
    void returnsFromGraveyardAfterProliferatingWithoutChoosingAPermanent() {
        var hybrid = new VoidwingHybrid();
        gd.playerGraveyards.get(player1.getId()).add(hybrid);

        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(hybrid);
        harness.assertInHand(player1, "Voidwing Hybrid");
    }
}
