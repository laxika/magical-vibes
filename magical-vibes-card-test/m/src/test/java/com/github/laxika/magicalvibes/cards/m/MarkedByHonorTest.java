package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({MarkedByHonor.class, RuneclawBear.class, DarksteelCitadel.class})
class MarkedByHonorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Marked by Honor attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new MarkedByHonor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof MarkedByHonor
                        && p.isAttached()
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and vigilance")
    void enchantedCreatureGetsBoostAndVigilance() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MarkedByHonor());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Marked by Honor affects only its enchanted creature")
    void doesNotAffectOtherCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MarkedByHonor());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses Marked by Honor's bonuses when it is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MarkedByHonor());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Marked by Honor cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        harness.setHand(player1, List.of(new MarkedByHonor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Marked by Honor can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new MarkedByHonor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof MarkedByHonor
                        && bear.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Two copies stack their boosts and removing one leaves the other active")
    void multipleCopiesStack() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new MarkedByHonor());
        firstAura.setAttachedTo(bear.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new MarkedByHonor());
        secondAura.setAttachedTo(bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstAura);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }
}
