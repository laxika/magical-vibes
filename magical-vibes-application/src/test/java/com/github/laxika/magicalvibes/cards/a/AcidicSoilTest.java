package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AcidicSoil.class, Forest.class, Mountain.class})
class AcidicSoilTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to each player equal to their land count")
    void dealsDamageBasedOnEachPlayersLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new AcidicSoil()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals no damage to a player who controls no lands")
    void noLandsMeansNoDamage() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new AcidicSoil()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts lands at resolution")
    void countsLandsAtResolution() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new AcidicSoil()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 0);

        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
