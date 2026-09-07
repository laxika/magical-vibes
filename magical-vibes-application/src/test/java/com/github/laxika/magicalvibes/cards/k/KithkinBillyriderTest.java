package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KithkinBillyrider.class)
class KithkinBillyriderTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals damage in both combat damage steps")
    void doubleStrikeDealsDamageInBothCombatDamageSteps() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KithkinBillyrider());

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
