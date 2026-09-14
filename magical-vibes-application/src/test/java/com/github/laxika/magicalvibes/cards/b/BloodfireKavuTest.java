package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoalitionHonorGuard;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodfireKavu.class, GaeasSkyfolk.class, CoalitionHonorGuard.class, YavimayaCoast.class})
class BloodfireKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to each creature")
    void sacrificesItselfAndDamagesEachCreature() {
        harness.addToBattlefield(player1, new BloodfireKavu());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        harness.addToBattlefield(player2, new GaeasSkyfolk());
        harness.addToBattlefield(player2, new CoalitionHonorGuard());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Bloodfire Kavu");
        harness.assertInGraveyard(player1, "Bloodfire Kavu");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");
        harness.assertNotOnBattlefield(player2, "Gaea's Skyfolk");
        harness.assertOnBattlefield(player2, "Coalition Honor Guard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not affect noncreature permanents")
    void doesNotAffectNoncreaturePermanents() {
        harness.addToBattlefield(player1, new BloodfireKavu());
        harness.addToBattlefield(player2, new YavimayaCoast());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Yavimaya Coast");
    }

    @Test
    @DisplayName("Requires one red mana")
    void requiresOneRedMana() {
        harness.addToBattlefield(player1, new BloodfireKavu());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bloodfire Kavu");
        harness.assertNotInGraveyard(player1, "Bloodfire Kavu");
    }
}
