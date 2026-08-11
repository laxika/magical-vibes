package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShimmerwildsGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color changes the enchanted land and adds that color when it produces mana")
    void chosenColorChangesLandAndAddsMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ShimmerwildsGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ShimmerwildsGrowth)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(forest.getId());
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.BLUE);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
    }

    @Test
    @DisplayName("The enchanted land's controller gets the bonus even when the Aura is controlled by an opponent")
    void enchantedLandControllerGetsBonus() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        Permanent aura = new Permanent(new ShimmerwildsGrowth());
        aura.setChosenColor(CardColor.BLUE);
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the enchanted land gets the additional mana")
    void onlyEnchantedLandGetsBonus() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent enchantedForest = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent aura = new Permanent(new ShimmerwildsGrowth());
        aura.setChosenColor(CardColor.BLUE);
        aura.setAttachedTo(enchantedForest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast Shimmerwilds Growth targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new ShimmerwildsGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
