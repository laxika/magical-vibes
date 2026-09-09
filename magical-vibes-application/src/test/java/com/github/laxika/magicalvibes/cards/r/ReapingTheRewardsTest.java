package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReapingTheRewards.class, CityOfTraitors.class, RagingGoblin.class})
class ReapingTheRewardsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reaping the Rewards gains 2 life and goes to the graveyard")
    void gainsLifeWithoutBuyback() {
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new ReapingTheRewards(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
        harness.assertInGraveyard(player1, "Reaping the Rewards");
    }

    @Test
    @DisplayName("Buyback is optional when a land is available")
    void canDeclineBuybackWithLandAvailable() {
        harness.addToBattlefield(player1, new CityOfTraitors());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ReapingTheRewards(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
        harness.assertInGraveyard(player1, "Reaping the Rewards");
        harness.assertOnBattlefield(player1, "City of Traitors");
    }

    @Test
    @DisplayName("Paying buyback sacrifices a land, gains 2 life, and returns Reaping the Rewards to hand")
    void buybackSacrificesLandGainsLifeAndReturnsToHand() {
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.setHand(player1, List.of(new ReapingTheRewards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castInstantWithSacrificeAndBuyback(
                player1, 0, null, harness.getPermanentId(player1, "City of Traitors"));

        assertThat(gd.stack.getFirst().isBuyback()).isTrue();
        assertThat(findPermanents(player1, "City of Traitors")).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
        harness.assertInHand(player1, "Reaping the Rewards");
        harness.assertNotInGraveyard(player1, "Reaping the Rewards");
    }

    @Test
    @DisplayName("Buyback cannot sacrifice a nonland permanent")
    void buybackRequiresLand() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ReapingTheRewards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrificeAndBuyback(
                player1, 0, null, harness.getPermanentId(player1, "Raging Goblin")))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Reaping the Rewards");
        harness.assertOnBattlefield(player1, "Raging Goblin");
    }
}
