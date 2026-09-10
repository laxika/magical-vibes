package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HerosResolve.class, CanopySpider.class, CursedScroll.class})
class HerosResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Hero's Resolve targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());

        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, spider.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Hero's Resolve attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());

        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hero's Resolve")
                        && spider.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+5")
    void enchantedCreatureGetsBuff() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HerosResolve());
        aura.setAttachedTo(spider.getId());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(8);
    }

    @Test
    @DisplayName("Hero's Resolve can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());

        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(8);
    }

    @Test
    @DisplayName("Creature returns to base stats when Hero's Resolve is removed")
    void effectsStopWhenRemoved() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HerosResolve());
        aura.setAttachedTo(spider.getId());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Hero's Resolve fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());

        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, spider.getId());
        gd.playerBattlefields.get(player1.getId()).remove(spider);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hero's Resolve");
        harness.assertNotOnBattlefield(player1, "Hero's Resolve");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Hero's Resolve")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
