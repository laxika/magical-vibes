package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwinscrollShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals combat damage in both combat-damage steps")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent shaman = addCreatureReady(player1, new TwinscrollShaman());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shaman);
    }
}
