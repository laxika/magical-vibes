package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HopeAgainstHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each creature its Aura controller controls")
    void boostsByCreaturesControlledByAuraController() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        Permanent aura = new Permanent(new HopeAgainstHope());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);

        gd.playerBattlefields.get(player1.getId()).remove(secondCreature);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstCreature);
    }

    @Test
    @DisplayName("Enchanted Human has first strike")
    void enchantedHumanHasFirstStrike() {
        Permanent target = addCreatureReady(player2, new HonorGuard());
        Permanent aura = new Permanent(new HopeAgainstHope());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted non-Human does not have first strike")
    void enchantedNonHumanDoesNotHaveFirstStrike() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new HopeAgainstHope());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HopeAgainstHope()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
