package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LightningElemental.class)
class LightningElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately after entering because of haste")
    void canAttackImmediatelyBecauseOfHaste() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LightningElemental());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
