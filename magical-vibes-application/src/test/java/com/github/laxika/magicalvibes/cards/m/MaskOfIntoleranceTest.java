package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({MaskOfIntolerance.class, Plains.class, Island.class, Swamp.class, Mountain.class, Forest.class})
class MaskOfIntoleranceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage at upkeep when the active player controls four basic land types")
    void dealsDamageWithFourBasicLandTypes() {
        harness.addToBattlefield(player1, new MaskOfIntolerance());
        addFourBasicLandTypes(player2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not deal damage when the active player controls fewer than four basic land types")
    void doesNotDealDamageWithFewerThanFourBasicLandTypes() {
        harness.addToBattlefield(player1, new MaskOfIntolerance());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Triggers during the controller's own upkeep")
    void triggersDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new MaskOfIntolerance());
        addFourBasicLandTypes(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    private void addFourBasicLandTypes(Player player) {
        harness.addToBattlefield(player, new Plains());
        harness.addToBattlefield(player, new Island());
        harness.addToBattlefield(player, new Swamp());
        harness.addToBattlefield(player, new Mountain());
    }
}
