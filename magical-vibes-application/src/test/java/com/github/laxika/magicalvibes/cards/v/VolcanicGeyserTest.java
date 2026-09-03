package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VolcanicGeyser.class, GoblinEliteInfantry.class})
class VolcanicGeyserTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to a target player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals no damage to a target player when X is zero")
    void dealsNoDamageWhenXIsZero() {
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals X damage to a target creature, destroying a 2/2")
    void dealsXDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinEliteInfantry());
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Elite Infantry");
        harness.assertInGraveyard(player2, "Goblin Elite Infantry");
    }
}
