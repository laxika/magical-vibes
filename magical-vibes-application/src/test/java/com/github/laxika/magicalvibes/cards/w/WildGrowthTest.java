package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildGrowth.class, Forest.class, GrizzlyBears.class})
class WildGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Wild Growth attaches it to target land")
    void resolvingAttachesToTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WildGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> forest.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Tapping enchanted Forest adds an additional {G}")
    void enchantedLandAddsExtraMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = new Permanent(new WildGrowth());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only enchanted land gets Wild Growth bonus")
    void onlyEnchantedLandGetsBonus() {
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent aura = new Permanent(new WildGrowth());
        aura.setAttachedTo(firstForest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // Tap second (non-enchanted) Forest at index 1.
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller of enchanted land gets bonus mana even if aura is controlled by opponent")
    void enchantedLandControllerGetsBonus() {
        Permanent opponentsForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = new Permanent(new WildGrowth());
        aura.setAttachedTo(opponentsForest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Each Wild Growth on the same land adds its own bonus")
    void multipleAurasEachAddBonus() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent firstAura = new Permanent(new WildGrowth());
        firstAura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(firstAura);
        Permanent secondAura = new Permanent(new WildGrowth());
        secondAura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(secondAura);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wild Growth bonus stops when aura leaves battlefield")
    void bonusStopsWhenAuraLeavesBattlefield() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = new Permanent(new WildGrowth());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast Wild Growth targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WildGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
