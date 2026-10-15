package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.c.CorrosiveOoze;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dub.class, CorrosiveOoze.class, ShortSword.class})
class DubTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dub puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dub");
    }

    @Test
    @DisplayName("Resolving Dub attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dub")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        harness.addToBattlefieldAndReturn(player1, new Dub()).setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature has first strike")
    void enchantedCreatureHasFirstStrike() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        harness.addToBattlefieldAndReturn(player1, new Dub()).setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature gains Knight subtype")
    void enchantedCreatureGainsKnightSubtype() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        harness.addToBattlefieldAndReturn(player1, new Dub()).setAttachedTo(bearsPerm.getId());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(bonus.subtypeOverriding()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature retains its original subtypes")
    void enchantedCreatureRetainsOriginalSubtypes() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        harness.addToBattlefieldAndReturn(player1, new Dub()).setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, bearsPerm, CardSubtype.OOZE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bearsPerm, CardSubtype.KNIGHT)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost, first strike, and Knight subtype when Dub is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        Permanent dubPerm = harness.addToBattlefieldAndReturn(player1, new Dub());
        dubPerm.setAttachedTo(bearsPerm.getId());

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isTrue();
        GameQueryService.StaticBonus bonusBefore = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonusBefore.grantedSubtypes()).contains(CardSubtype.KNIGHT);

        // Remove Dub
        gd.playerBattlefields.get(player1.getId()).remove(dubPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isFalse();
        GameQueryService.StaticBonus bonusAfter = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonusAfter.grantedSubtypes()).doesNotContain(CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("Can target a creature with Dub")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());
        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Dub")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new CorrosiveOoze());
        harness.addToBattlefield(player1, new ShortSword());
        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Short Sword");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Dub does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());

        harness.addToBattlefieldAndReturn(player1, new Dub()).setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isFalse();
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, otherBears);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("Dub can enchant an opponent's creature and grants all its effects")
    void enchantsOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CorrosiveOoze());
        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dub").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.KNIGHT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.OOZE)).isTrue();
    }

    @Test
    @DisplayName("Two Dubs stack their boosts and removing one preserves the other's effects")
    void multipleDubsStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CorrosiveOoze());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Dub());
        first.setAttachedTo(creature.getId());
        harness.addToBattlefieldAndReturn(player1, new Dub()).setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.KNIGHT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.OOZE)).isTrue();
    }
}
