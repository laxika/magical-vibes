package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RollingEarthquake.class, ShuFootSoldiers.class, ShuCavalry.class})
class RollingEarthquakeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to creatures without horsemanship")
    void damagesNonHorsemanshipCreatures() {
        addCreatureReady(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 3);

        // Shu Foot Soldiers (2/3) takes 3 damage and dies.
        harness.assertNotOnBattlefield(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Does not damage creatures with horsemanship")
    void doesNotDamageHorsemanshipCreatures() {
        addCreatureReady(player2, new ShuCavalry());

        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 2);

        // Horsemanship creature survives.
        harness.assertOnBattlefield(player2, "Shu Cavalry");
    }

    @Test
    @DisplayName("Deals X damage to each player")
    void damagesEachPlayer() {
        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        addCreatureReady(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Shu Foot Soldiers");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Nonlethal damage accumulates on both sides while horsemanship creatures remain unharmed")
    void damagesBothSidesAndAccumulatesDamage() {
        addCreatureReady(player1, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player1, new ShuCavalry());
        addCreatureReady(player2, new ShuCavalry());

        harness.setHand(player1, List.of(new RollingEarthquake(), new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertOnBattlefield(player1, "Shu Foot Soldiers");
        harness.assertOnBattlefield(player2, "Shu Foot Soldiers");
        harness.assertOnBattlefield(player1, "Shu Cavalry");
        harness.assertOnBattlefield(player2, "Shu Cavalry");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertNotOnBattlefield(player1, "Shu Foot Soldiers");
        harness.assertNotOnBattlefield(player2, "Shu Foot Soldiers");
        harness.assertOnBattlefield(player1, "Shu Cavalry");
        harness.assertOnBattlefield(player2, "Shu Cavalry");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }
}
