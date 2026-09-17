package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinisterStrength.class, AlphaKavu.class, ManaCylix.class})
class SinisterStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sinister Strength attaches it, boosts the creature, and makes it black")
    void castAttachesBoostsAndMakesBlack() {
        Permanent creature = addCreatureReady(player1, new AlphaKavu());

        harness.setHand(player1, List.of(new SinisterStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sinister Strength")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasColor(gd, creature, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Boost and color change are lost when Sinister Strength leaves the battlefield")
    void effectsLostWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new AlphaKavu());
        Permanent aura = new Permanent(new SinisterStrength());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasColor(gd, creature, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasColor(gd, creature, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Sinister Strength")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ManaCylix());
        harness.setHand(player1, List.of(new SinisterStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = findPermanent(player1, "Mana Cylix");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
