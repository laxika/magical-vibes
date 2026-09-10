package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        GameData gd = harness.getGameData();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        addCreatureReady(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();

        harness.assertOnBattlefield(player2, "Shu Foot Soldiers");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
