package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrazenScourge.class})
class BrazenScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Brazen Scourge to attack and deal damage while summoning sick")
    void hasteAllowsAttackWhileSummoningSick() {
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new BrazenScourge());
        assertThat(scourge.isSummoningSick()).isTrue();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Brazen Scourge can attack on the turn it is cast")
    void canAttackOnTurnItIsCast() {
        harness.setHand(player1, List.of(new BrazenScourge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Brazen Scourge");

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }
}
