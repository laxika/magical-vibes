package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParallaxDementiaTest extends BaseCardTest {

    @Test
    @DisplayName("Parallax Dementia enters with a fade counter and boosts the enchanted creature")
    void entersWithFadeCounterAndBoostsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ParallaxDementia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Parallax Dementia");
        assertThat(aura.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Parallax Dementia removes its fade counter at upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ParallaxDementia());
        aura.setAttachedTo(bears.getId());
        aura.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Parallax Dementia");
    }

    @Test
    @DisplayName("Parallax Dementia sacrifices itself at upkeep with no fade counters")
    void sacrificesWithNoFadeCounters() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ParallaxDementia());
        aura.setAttachedTo(bears.getId());
        aura.setCounterCount(CounterType.FADE, 0);

        advanceToUpkeep(player1);
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Parallax Dementia");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("When Parallax Dementia leaves, it destroys the enchanted creature without regeneration")
    void destroysEnchantedCreatureWhenAuraLeaves() {
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ParallaxDementia());
        aura.setAttachedTo(skeletons.getId());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, aura.getId());
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player2, "Drudge Skeletons");
        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Parallax Dementia cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new ParallaxDementia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
