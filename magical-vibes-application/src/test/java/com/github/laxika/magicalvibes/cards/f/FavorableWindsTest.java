package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OneWithTheWind;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FavorableWinds.class, CloudElemental.class, GrizzlyBears.class,
        OneWithTheWind.class, Opalescence.class})
class FavorableWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Own creature with flying gets +1/+1")
    void ownFlyingCreatureGetsBoosted() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new CloudElemental());

        Permanent elemental = findPermanent(player1, "Cloud Elemental");

        // Cloud Elemental is 2/3; with +1/+1 boost = 3/4
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost creature without flying")
    void doesNotBoostNonFlyingCreature() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        // Grizzly Bears is 2/2, no flying, should not be boosted
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's flying creature")
    void doesNotBoostOpponentFlyingCreature() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player2, new CloudElemental());

        Permanent elemental = findPermanent(player2, "Cloud Elemental");

        // Opponent's Cloud Elemental should remain 2/3
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost is lost when Favorable Winds leaves the battlefield")
    void boostLostWhenEnchantmentRemoved() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new CloudElemental());

        Permanent elemental = findPermanent(player1, "Cloud Elemental");
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);

        // Remove the enchantment
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Favorable Winds"));

        // Elemental should revert to 2/3
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Favorable Winds stack")
    void multipleWindsStack() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new CloudElemental());

        Permanent elemental = findPermanent(player1, "Cloud Elemental");

        // Cloud Elemental is 2/3; with two +1/+1 boosts = 4/5
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }

    @Test
    @DisplayName("A creature gaining flying receives the boost and loses it when flying is lost")
    void boostTracksGrantedFlying() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.setHand(player1, List.of(new OneWithTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "One With the Wind"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Favorable Winds boosts itself when it becomes a creature with flying")
    void animatedWindsWithFlyingBoostsItself() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new Opalescence());
        Permanent winds = findPermanent(player1, "Favorable Winds");
        assertThat(gqs.getEffectivePower(gd, winds)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, winds)).isEqualTo(2);

        harness.setHand(player1, List.of(new OneWithTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, winds.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, winds)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, winds)).isEqualTo(5);
    }
}
