package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class BrazenScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Brazen Scourge to attack and deal damage while summoning sick")
    void hasteAllowsAttackWhileSummoningSick() {
        Permanent scourge = new Permanent(new BrazenScourge());
        gd.playerBattlefields.get(player1.getId()).add(scourge);
        assertThat(scourge.isSummoningSick()).isTrue();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }
}
