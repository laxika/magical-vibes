package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlayerHuskTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Flayer Husk has living weapon ETB effect")
    void hasLivingWeaponEffect() {
        FlayerHusk card = new FlayerHusk();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(LivingWeaponEffect.class);
    }

    @Test
    @DisplayName("Flayer Husk has static +1/+1 boost")
    void hasStaticBoost() {
        FlayerHusk card = new FlayerHusk();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof BoostAttachedCreatureEffect)
                .hasSize(1);
        BoostAttachedCreatureEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BoostAttachedCreatureEffect)
                .map(e -> (BoostAttachedCreatureEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flayer Husk has equip {2} ability")
    void hasEquipAbility() {
        FlayerHusk card = new FlayerHusk();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Living weapon ETB =====

    @Test
    @DisplayName("Casting Flayer Husk triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // After the artifact resolves, the living weapon ETB should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Flayer Husk");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        // Resolve artifact spell
        harness.passBothPriorities();
        // Resolve living weapon ETB trigger
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        // Should have Flayer Husk (equipment) and Phyrexian Germ (token)
        Permanent flayerHusk = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Flayer Husk"))
                .findFirst().orElseThrow();
        Permanent germ = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // Flayer Husk should be attached to the Germ token
        assertThat(flayerHusk.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        assertThat(germ.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(germ.getCard().getPower()).isEqualTo(0);
        assertThat(germ.getCard().getToughness()).isEqualTo(0);
        assertThat(germ.getCard().isToken()).isTrue();
        assertThat(germ.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GERM);
    }

    // ===== Germ gets equipment bonuses =====

    @Test
    @DisplayName("Germ token gets +1/+1 from Flayer Husk")
    void germGetsEquipmentBonuses() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // 0/0 base + 1/1 from equipment = 1/1 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    // ===== Equip to another creature =====

    @Test
    @DisplayName("Equipping Flayer Husk to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a creature to equip to
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent flayerHusk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Flayer Husk"))
                .findFirst().orElseThrow();

        assertThat(flayerHusk.getAttachedTo()).isEqualTo(bears.getId());

        // Bears should get +1/+1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);  // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);  // 2 + 1
    }

    // ===== Germ dies when equipment is moved =====

    @Test
    @DisplayName("Germ token dies (0 toughness) when Flayer Husk is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears — this moves the equipment, Germ becomes 0/0 and dies
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness, state-based action)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Germ"));
    }
}
