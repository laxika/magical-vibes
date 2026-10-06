package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Geistwave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunawayGrowth.class, Forest.class, Geistwave.class})
class RunawayGrowthTest extends BaseCardTest {

    @Test
    void startsAtIntensityOneWhenItEnters() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        RunawayGrowth growth = new RunawayGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void addsCurrentIntensityThenIntensifies() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castGrowthOn(forest);

        harness.tapPermanent(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        forest.setTapped(false);
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    void unrelatedLandDoesNotProduceBonusManaOrIntensify() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castGrowthOn(forest);

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void bonusManaGoesToEnchantedLandsController() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castGrowthOn(forest);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @CardUsed(Geistwave.class)
    void intensityIsRetainedWhenBouncedAndRecast() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castGrowthOn(forest);
        harness.tapPermanent(player1, 0);
        resolveAllTriggers();
        Permanent aura = findPermanent(player1, "Runaway Growth");

        harness.setHand(player2, List.of(new Geistwave()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();
        gd.playerManaPools.get(player1.getId()).clear();
        forest.setTapped(false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    private void castGrowthOn(Permanent land) {
        harness.setHand(player1, List.of(new RunawayGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, land.getId());
        resolveAllTriggers();
    }
}
