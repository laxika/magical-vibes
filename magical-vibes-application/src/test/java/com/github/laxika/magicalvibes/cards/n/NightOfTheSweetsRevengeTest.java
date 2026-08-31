package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightOfTheSweetsRevenge.class, GrizzlyBears.class})
class NightOfTheSweetsRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters the battlefield")
    void createsFoodOnEntry() {
        castNight();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Foods you control can tap to add green mana")
    void foodCanTapForGreenMana() {
        castNight();

        Permanent food = findPermanent(player1, "Food");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food),
                1, null, null);

        assertThat(food.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing it boosts your creatures by the number of Foods you control")
    void sacrificeBoostsCreaturesByFoodCount() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent night = castNight();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(night), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(night);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    private Permanent castNight() {
        harness.setHand(player1, List.of(new NightOfTheSweetsRevenge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Night of the Sweets' Revenge");
    }
}
