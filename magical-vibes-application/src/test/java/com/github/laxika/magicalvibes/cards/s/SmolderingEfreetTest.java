package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmolderingEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("When Smoldering Efreet dies, it deals 2 damage to its controller")
    void dealsDamageToItsControllerWhenItDies() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new SmolderingEfreet());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, efreet.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Smoldering Efreet does not deal damage while it remains on the battlefield")
    void doesNotDealDamageWithoutDying() {
        harness.addToBattlefield(player1, new SmolderingEfreet());
        harness.setLife(player1, 20);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
