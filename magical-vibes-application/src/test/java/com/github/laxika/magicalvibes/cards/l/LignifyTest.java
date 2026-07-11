package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LignifyTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Lignify and resolving attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

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

    // ===== Base P/T override =====

    @Test
    @DisplayName("Enchanted creature has base power and toughness 0/4")
    void setsBasePowerToughness() {
        // Air Elemental is a 4/4 with flying
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        Permanent lignifyPerm = new Permanent(new Lignify());
        lignifyPerm.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(lignifyPerm);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
    }

    // ===== Loses all abilities =====

    @Test
    @DisplayName("Enchanted creature loses its original keywords like flying")
    void losesOriginalKeywords() {
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        Permanent lignifyPerm = new Permanent(new Lignify());
        lignifyPerm.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(lignifyPerm);

        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature with activated ability cannot use it")
    void losesActivatedAbilities() {
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        Permanent lignifyPerm = new Permanent(new Lignify());
        lignifyPerm.setAttachedTo(pyromancer.getId());
        gd.playerBattlefields.get(player2.getId()).add(lignifyPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Type change =====

    @Test
    @DisplayName("Enchanted creature becomes a Treefolk, replacing its other creature types")
    void becomesTreefolkReplacingTypes() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent lignifyPerm = new Permanent(new Lignify());
        lignifyPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(lignifyPerm);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.TREEFOLK);
        assertThat(bonus.subtypeOverriding()).isTrue();
    }

    // ===== Removal restores everything =====

    @Test
    @DisplayName("Removing Lignify restores creature's original P/T and abilities")
    void removalRestoresOriginalState() {
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        Permanent lignifyPerm = new Permanent(new Lignify());
        lignifyPerm.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(lignifyPerm);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(lignifyPerm);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Lignify")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        com.github.laxika.magicalvibes.cards.f.FountainOfYouth artifact = new com.github.laxika.magicalvibes.cards.f.FountainOfYouth();
        harness.addToBattlefield(player1, artifact);
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifactPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
