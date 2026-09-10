package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RagingMinotaur.class)
class RagingMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Raging Minotaur to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RagingMinotaur()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
