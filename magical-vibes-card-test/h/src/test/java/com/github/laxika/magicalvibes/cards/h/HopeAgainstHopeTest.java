package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.cards.t.TrueFaithCenser;
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

@CardUsed({HopeAgainstHope.class, GrizzlyBears.class, HonorGuard.class, FountainOfYouth.class,
        DevilthornFox.class, ThrabenInspector.class, TrueFaithCenser.class})
class HopeAgainstHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each creature its Aura controller controls")
    void boostsByCreaturesControlledByAuraController() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HopeAgainstHope());
        aura.setAttachedTo(target.getId());

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
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HopeAgainstHope());
        aura.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted non-Human does not have first strike")
    void enchantedNonHumanDoesNotHaveFirstStrike() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HopeAgainstHope());
        aura.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HopeAgainstHope()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving the Aura counts its own enchanted creature but not artifacts or opposing creatures")
    void resolvedAuraCountsEnchantedCreatureAndUpdatesWhenCreaturesEnter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThrabenInspector());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.addToBattlefield(player2, new DevilthornFox());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);
        harness.setHand(player1, List.of(new HopeAgainstHope()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hope Against Hope").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        Permanent other = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opposing Human still gains first strike when the Aura controller controls no creatures")
    void zeroCreaturesDoesNotPreventFirstStrike() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThrabenInspector());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);
        harness.setHand(player1, List.of(new HopeAgainstHope()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }
}
