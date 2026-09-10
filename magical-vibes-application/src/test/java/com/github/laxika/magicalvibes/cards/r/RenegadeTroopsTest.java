package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RenegadeTroops.class)
class RenegadeTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Renegade Troops to attack and deal damage the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.setLife(player2, 20);
        Permanent troops = harness.addToBattlefieldAndReturn(player1, new RenegadeTroops());

        assertThat(troops.isSummoningSick()).isTrue();

        declareAttackers(List.of(0));
        assertThat(troops.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
