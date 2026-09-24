package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TirelessProvisioner.class, Forest.class})
class TirelessProvisionerTest extends BaseCardTest {

    private static final String FOOD_MODE = "Create a Food token";
    private static final String TREASURE_MODE = "Create a Treasure token";

    @Test
    void landfallCreatesFoodTokenWhenChosen() {
        harness.addToBattlefield(player1, new TirelessProvisioner());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, FOOD_MODE);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void landfallCreatesTreasureTokenWhenChosen() {
        harness.addToBattlefield(player1, new TirelessProvisioner());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void opponentLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new TirelessProvisioner());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isZero();
    }
}
