package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SickleslicerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sickleslicer has living weapon ETB effect")
    void hasLivingWeaponEffect() {
        Sickleslicer card = new Sickleslicer();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(LivingWeaponEffect.class);
    }

    @Test
    @DisplayName("Sickleslicer has static +2/+2 boost")
    void hasStaticBoost() {
        Sickleslicer card = new Sickleslicer();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof StaticBoostEffect)
                .hasSize(1);
        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sickleslicer has equip {4} ability")
    void hasEquipAbility() {
        Sickleslicer card = new Sickleslicer();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{4}");
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
    @DisplayName("Casting Sickleslicer triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // After the artifact resolves, the living weapon ETB should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Sickleslicer");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        Permanent sickleslicer = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Sickleslicer"))
                .findFirst().orElseThrow();
        Permanent germ = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        assertThat(sickleslicer.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

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
    @DisplayName("Germ token gets +2/+2 from Sickleslicer")
    void germGetsEquipmentBonuses() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // 0/0 base + 2/2 from equipment = 2/2 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);
    }

    // ===== Equip to another creature =====

    @Test
    @DisplayName("Equipping Sickleslicer to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a creature to equip to
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent sickleslicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sickleslicer"))
                .findFirst().orElseThrow();

        assertThat(sickleslicer.getAttachedTo()).isEqualTo(bears.getId());

        // Bears should get +2/+2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);  // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);  // 2 + 2
    }

    // ===== Germ dies when equipment is moved =====

    @Test
    @DisplayName("Germ token dies (0 toughness) when Sickleslicer is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears — this moves the equipment, Germ becomes 0/0 and dies
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness, state-based action)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Germ"));
    }
}
