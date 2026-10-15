package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ToughCookie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightOfTheSweetsRevenge.class, GrizzlyBears.class, ToughCookie.class})
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

    @Test
    void foodRetainsItsLifeGainAbility() {
        castNight();
        Permanent food = findPermanent(player1, "Food");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food),
                0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void nonTokenFoodCanTapForGreenMana() {
        Permanent cookie = addCreatureReady(player1, new ToughCookie());
        castNight();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cookie),
                1, null, null);

        assertThat(cookie.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostCountsOwnFoodsAtResolutionAndIncludesNonTokenFoods() {
        Permanent cookie = addCreatureReady(player1, new ToughCookie());
        Permanent opponentCookie = addCreatureReady(player2, new ToughCookie());
        Permanent night = castNight();
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(night), null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(night);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food),
                0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, cookie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cookie)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCookie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCookie)).isEqualTo(2);

        Permanent laterCookie = addCreatureReady(player1, new ToughCookie());
        assertThat(gqs.getEffectivePower(gd, laterCookie)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, cookie)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, cookie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cookie)).isEqualTo(2);
    }

    @Test
    void cannotActivateBoostOutsideMainPhase() {
        Permanent night = castNight();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(night), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(night);
    }

    private Permanent castNight() {
        harness.setHand(player1, List.of(new NightOfTheSweetsRevenge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Night of the Sweets' Revenge");
    }
}
