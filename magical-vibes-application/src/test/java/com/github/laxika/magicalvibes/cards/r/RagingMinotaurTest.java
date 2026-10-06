package com.github.laxika.magicalvibes.cards.r;

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
        harness.castFromHand(player1, new RagingMinotaur(), "{2}{R}{R}");
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Haste does not allow a tapped Raging Minotaur to attack")
    void hasteDoesNotAllowAttackingWhileTapped() {
        harness.castFromHand(player1, new RagingMinotaur(), "{2}{R}{R}");
        harness.passBothPriorities();

        var minotaur = findPermanent(player1, "Raging Minotaur");
        assertThat(als.canAttack(gd, minotaur, player1.getId())).isTrue();

        minotaur.tap();

        assertThat(als.canAttack(gd, minotaur, player1.getId())).isFalse();
    }
}
