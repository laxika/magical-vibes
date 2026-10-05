package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lignify.class, AirElemental.class, GrizzlyBears.class, ProdigalPyromancer.class, FountainOfYouth.class})
class LignifyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Lignify and resolving attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lignify")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has base power and toughness 0/4")
    void setsBasePowerToughness() {
        // Air Elemental is a 4/4 with flying
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        airElemental.setSummoningSick(false);

        Permanent lignifyPerm = harness.addToBattlefieldAndReturn(player1, new Lignify());
        lignifyPerm.setAttachedTo(airElemental.getId());

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature loses its original keywords like flying")
    void losesOriginalKeywords() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        airElemental.setSummoningSick(false);

        Permanent lignifyPerm = harness.addToBattlefieldAndReturn(player1, new Lignify());
        lignifyPerm.setAttachedTo(airElemental.getId());

        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature with activated ability cannot use it")
    void losesActivatedAbilities() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        Permanent lignifyPerm = harness.addToBattlefieldAndReturn(player2, new Lignify());
        lignifyPerm.setAttachedTo(pyromancer.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature becomes a Treefolk, replacing its other creature types")
    void becomesTreefolkReplacingTypes() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent lignifyPerm = harness.addToBattlefieldAndReturn(player1, new Lignify());
        lignifyPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, bearsPerm, CardSubtype.TREEFOLK)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bearsPerm, CardSubtype.BEAR)).isFalse();
    }

    @Test
    @DisplayName("Removing Lignify restores creature's original P/T and abilities")
    void removalRestoresOriginalState() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        airElemental.setSummoningSick(false);

        Permanent lignifyPerm = harness.addToBattlefieldAndReturn(player1, new Lignify());
        lignifyPerm.setAttachedTo(airElemental.getId());

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(lignifyPerm);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Lignify")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent artifactPerm = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Lignify preserves counters and affects only the enchanted creature")
    void preservesCountersAndLeavesOtherCreaturesUnchanged() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        enchanted.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Lignify());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.TREEFOLK)).isFalse();
    }

    @Test
    @DisplayName("Lignify goes to the graveyard when its target leaves before resolution")
    void targetLeavingBeforeResolutionPreventsAttachment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Lignify");
        harness.assertInGraveyard(player1, "Lignify");
    }
    @Test
    @DisplayName("Removing Lignify restores original creature types")
    void removalRestoresCreatureTypes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Lignify());
        aura.setAttachedTo(creature.getId());
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.TREEFOLK)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.BEAR)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.TREEFOLK)).isFalse();
    }
}
