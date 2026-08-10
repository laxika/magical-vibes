package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReapingTheRewardsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reaping the Rewards gains 2 life and goes to the graveyard")
    void gainsLifeWithoutBuyback() {
        harness.setHand(player1, List.of(new ReapingTheRewards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
        harness.assertInGraveyard(player1, "Reaping the Rewards");
    }

    @Test
    @DisplayName("Paying buyback sacrifices a land, gains 2 life, and returns Reaping the Rewards to hand")
    void buybackSacrificesLandGainsLifeAndReturnsToHand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new ReapingTheRewards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castInstantWithSacrificeAndBuyback(
                player1, 0, null, harness.getPermanentId(player1, "Forest"));

        assertThat(gd.stack.getFirst().isBuyback()).isTrue();
        assertThat(findPermanents(player1, "Forest")).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
        harness.assertInHand(player1, "Reaping the Rewards");
        harness.assertNotInGraveyard(player1, "Reaping the Rewards");
    }

    @Test
    @DisplayName("Buyback cannot sacrifice a nonland permanent")
    void buybackRequiresLand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReapingTheRewards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrificeAndBuyback(
                player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Reaping the Rewards");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
