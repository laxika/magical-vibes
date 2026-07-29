package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgilityTest extends BaseCardTest {

    private Permanent enchantedBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new Agility());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return bears;
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has flanking")
    void enchantedCreatureBoostedAndHasFlanking() {
        Permanent bears = enchantedBears();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flanking end when the Aura leaves the battlefield")
    void effectsEndWhenAuraLeaves() {
        Permanent bears = enchantedBears();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof Agility);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new Agility()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent land = findPermanent(player1, "Plains");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
