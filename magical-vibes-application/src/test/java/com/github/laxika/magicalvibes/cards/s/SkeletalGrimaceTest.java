package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletalGrimace.class, WalkingCorpse.class, TravelersAmulet.class, BrimstoneVolley.class})
class SkeletalGrimaceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Skeletal Grimace puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new SkeletalGrimace()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(SkeletalGrimace.class);
    }

    @Test
    @DisplayName("Resolving Skeletal Grimace attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new SkeletalGrimace()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Skeletal Grimace")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature can activate regeneration")
    void grantedAbilityGrantsRegeneration() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate regeneration multiple times to stack shields")
    void canActivateRegenerationMultipleTimes() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.getRegenerationShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateWithoutMana() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating regeneration does not tap the creature")
    void regenerationDoesNotTap() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature loses boost and regeneration ability when Skeletal Grimace is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(3);

        // Remove Skeletal Grimace
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);

        // Creature should no longer have an activated ability
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Can target a creature with Skeletal Grimace")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new SkeletalGrimace()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Skeletal Grimace")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.setHand(player1, List.of(new SkeletalGrimace()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = findPermanent(player1, "Traveler's Amulet");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Skeletal Grimace does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Other creature should not be affected
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
    }
    @Test
    @DisplayName("Regeneration replaces lethal damage and keeps the Aura attached")
    void regenerationReplacesLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new BrimstoneVolley()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The enchanted creature's controller can activate the granted ability")
    void opposingCreatureControllerCanRegenerate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new SkeletalGrimace()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted regeneration already on the stack resolves after the Aura leaves")
    void regenerationResolvesAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SkeletalGrimace());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(creature.isTapped()).isFalse();
    }
}
