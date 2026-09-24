package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TirelessProvisioner.class, Forest.class})
class TirelessProvisionerTest extends BaseCardTest {

    @Test
    void landfallCanCreateFood() {
        addProvisionerAndLand();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Create a Food token");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void landfallCanCreateTreasure() {
        addProvisionerAndLand();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Treasure token");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void addProvisionerAndLand() {
        harness.addToBattlefield(player1, new TirelessProvisioner());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
    }
}
