package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.ArcaneFlight;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.b.BenalishMarshal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepFreeze.class, AirElemental.class, GrizzlyBears.class, ProdigalPyromancer.class, FountainOfYouth.class, ArcaneFlight.class, BenalishMarshal.class})
class DeepFreezeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Deep Freeze and resolving attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Deep Freeze")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has base power and toughness 0/4")
    void setsBasePowerToughness() {
        // Air Elemental is a 4/4 with flying
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        airElemental.setSummoningSick(false);

        // Attach Deep Freeze directly
        Permanent deepFreezePerm = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreezePerm.setAttachedTo(airElemental.getId());

        int effectivePower = gqs.getEffectivePower(gd, airElemental);
        int effectiveToughness = gqs.getEffectiveToughness(gd, airElemental);

        assertThat(effectivePower).isEqualTo(0);
        assertThat(effectiveToughness).isEqualTo(4);
    }

    @Test
    @DisplayName("Counters still apply on top of Deep Freeze base P/T")
    void countersApplyOnTopOfBasePT() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        // Attach Deep Freeze
        Permanent deepFreezePerm = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreezePerm.setAttachedTo(bearsPerm.getId());

        // Base 0/4 + 2 counters = 2/6
        int effectivePower = gqs.getEffectivePower(gd, bearsPerm);
        int effectiveToughness = gqs.getEffectiveToughness(gd, bearsPerm);

        assertThat(effectivePower).isEqualTo(2);
        assertThat(effectiveToughness).isEqualTo(6);
    }

    @Test
    @DisplayName("Enchanted creature has defender")
    void grantsDefender() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        // Attach Deep Freeze
        Permanent deepFreezePerm = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreezePerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature loses its original keywords like flying")
    void losesOriginalKeywords() {
        // Air Elemental has flying
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        airElemental.setSummoningSick(false);

        // Attach Deep Freeze
        Permanent deepFreezePerm = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreezePerm.setAttachedTo(airElemental.getId());

        // Air Elemental should have lost flying
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
        // But should still have defender (granted by Deep Freeze)
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature with activated ability cannot use it")
    void losesActivatedAbilities() {
        // Prodigal Pyromancer has a tap ability
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        // Attach Deep Freeze
        Permanent deepFreezePerm = harness.addToBattlefieldAndReturn(player2, new DeepFreeze());
        deepFreezePerm.setAttachedTo(pyromancer.getId());

        // Attempting to activate the ability should fail
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature is blue in addition to its other colors")
    void grantsBlueColor() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        // Attach Deep Freeze
        Permanent deepFreezePerm = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreezePerm.setAttachedTo(bearsPerm.getId());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedColors()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Enchanted creature is a Wall in addition to its other types")
    void grantsWallSubtype() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        // Attach Deep Freeze
        Permanent deepFreezePerm = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreezePerm.setAttachedTo(bearsPerm.getId());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.WALL);
    }

    @Test
    @DisplayName("Removing Deep Freeze restores creature's original P/T and abilities")
    void removalRestoresOriginalState() {
        // Air Elemental is a 4/4 with flying
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        airElemental.setSummoningSick(false);

        // Attach Deep Freeze
        Permanent deepFreezePerm = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreezePerm.setAttachedTo(airElemental.getId());

        // Verify Deep Freeze effects are active
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.DEFENDER)).isTrue();

        // Remove Deep Freeze
        gd.playerBattlefields.get(player1.getId()).remove(deepFreezePerm);

        // Verify creature is back to normal
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Deep Freeze")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Add a noncreature permanent
        Permanent artifactPerm = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Deep Freeze preserves existing colors and creature types")
    void retainsOriginalColorsAndTypes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.WALL)).isTrue();
    }

    @Test
    @DisplayName("Deep Freeze does not remove an activated ability already on the stack")
    void alreadyActivatedAbilityStillResolves() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, player2.getId());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new DeepFreeze());
        aura.setAttachedTo(pyromancer.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deep Freeze does not resolve if its target has left the battlefield")
    void missingTargetPreventsResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, bears.getId());

        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deep Freeze");
        harness.assertInGraveyard(player1, "Deep Freeze");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying granted before Deep Freeze is removed while the power boost remains")
    void removesEarlierGrantedFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcaneFlight(), new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Flying granted after Deep Freeze is retained")
    void retainsLaterGrantedFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeepFreeze(), new ArcaneFlight()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Deep Freeze removes a creature's static ability affecting other creatures")
    void removesStaticAbilityAffectingOtherCreatures() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player2, new BenalishMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, marshal.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
