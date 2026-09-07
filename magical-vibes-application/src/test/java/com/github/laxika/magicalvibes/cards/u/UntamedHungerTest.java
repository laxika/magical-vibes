package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UntamedHunger.class, GrizzlyBears.class, FountainOfYouth.class})
class UntamedHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Untamed Hunger attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UntamedHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof UntamedHunger
                        && target.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+1 and menace")
    void enchantedCreatureGetsBoostAndMenace() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new UntamedHunger());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Effects stop when Untamed Hunger is removed")
    void effectsStopWhenRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new UntamedHunger());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Untamed Hunger does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new UntamedHunger());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Untamed Hunger fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UntamedHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Untamed Hunger");
        harness.assertNotOnBattlefield(player1, "Untamed Hunger");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Untamed Hunger")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new UntamedHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
