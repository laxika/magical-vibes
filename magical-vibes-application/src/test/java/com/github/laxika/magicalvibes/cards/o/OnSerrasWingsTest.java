package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({OnSerrasWings.class, BalothGorger.class, JoustingLance.class})
class OnSerrasWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting On Serra's Wings puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving On Serra's Wings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("On Serra's Wings")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has vigilance")
    void enchantedCreatureHasVigilance() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has lifelink")
    void enchantedCreatureHasLifelink() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature gains legendary supertype")
    void enchantedCreatureBecomesLegendary() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        assertThat(gqs.hasEffectiveSupertype(gd, bearsPerm, CardSupertype.LEGENDARY)).isFalse();

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasEffectiveSupertype(gd, bearsPerm, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    @DisplayName("Creature loses all bonuses when On Serra's Wings is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, bearsPerm, CardSupertype.LEGENDARY)).isTrue();

        // Remove On Serra's Wings
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasEffectiveSupertype(gd, bearsPerm, CardSupertype.LEGENDARY)).isFalse();
    }

    @Test
    @DisplayName("Can target a creature with On Serra's Wings")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with On Serra's Wings")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addToBattlefield(player1, new JoustingLance());
        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent artifact = findPermanent(player1, "Jousting Lance");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("On Serra's Wings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Other creature should not be affected
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.LIFELINK)).isFalse();
    }
    @Test
    @DisplayName("Can enchant an opposing creature and grant all bonuses")
    void enchantsOpposingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "On Serra's Wings");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasEffectiveSupertype(gd, creature, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Aura goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "On Serra's Wings");
        harness.assertInGraveyard(player1, "On Serra's Wings");
    }

    @Test
    @DisplayName("An enchanted creature can coexist with a nonlegendary creature of the same name")
    void sameNameNonlegendaryCreatureSurvives() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        aura.setAttachedTo(enchanted.getId());

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enchanted, other, aura);
        assertThat(gqs.hasEffectiveSupertype(gd, enchanted, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, other, CardSupertype.LEGENDARY)).isFalse();
    }

    @Test
    @DisplayName("Keeping one of two legendary Auras removes the other Aura's bonuses")
    void legendRuleRemovesDuplicateAuraAndItsBonuses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        Permanent keptAura = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        keptAura.setAttachedTo(first.getId());
        Permanent removedAura = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        removedAura.setAttachedTo(second.getId());

        harness.runStateBasedActions();
        harness.handlePermanentChosen(player1, keptAura.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keptAura).doesNotContain(removedAura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(removedAura.getCard());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSupertype(gd, second, CardSupertype.LEGENDARY)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isFalse();
    }
}
