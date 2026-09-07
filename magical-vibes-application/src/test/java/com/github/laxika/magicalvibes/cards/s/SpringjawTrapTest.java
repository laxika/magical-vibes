package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SpringjawTrap.class, GrizzlyBears.class})
class SpringjawTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to target creature")
    void sacrificesItselfAndDealsDamageToCreature() {
        harness.addToBattlefield(player1, new SpringjawTrap());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Springjaw Trap");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to target player")
    void sacrificesItselfAndDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new SpringjawTrap());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Springjaw Trap");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }
}
