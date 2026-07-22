package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbundantGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Abundant Growth attaches to land and controller draws a card")
    void resolvingAttachesAndDraws() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new AbundantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        // Resolve the aura spell — attaches to land, ETB trigger goes on stack
        harness.passBothPriorities();
        // Resolve the ETB trigger — draw a card
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Abundant Growth")
                        && forest.getId().equals(p.getAttachedTo()));
        // Hand emptied by casting, then drew 1 from library
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 1);
    }

    @Test
    @DisplayName("Enchanted land gains mana ability that produces one mana of any color")
    void enchantedLandGainsManaAbility() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new AbundantGrowth());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land can still produce its normal mana when tapped directly")
    void enchantedLandStillProducesNormalMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new AbundantGrowth());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted mana ability goes away when aura leaves battlefield")
    void manaAbilityRemovedWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new AbundantGrowth());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        var staticBonus = gqs.computeStaticBonus(gd, forest);
        assertThat(staticBonus.grantedActivatedAbilities()).isEmpty();
    }
}
