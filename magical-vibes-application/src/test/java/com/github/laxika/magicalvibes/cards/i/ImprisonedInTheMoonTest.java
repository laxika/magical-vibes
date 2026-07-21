package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImprisonedInTheMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches to target creature")
    void resolvingAttachesToCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Imprisoned in the Moon")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature is a colorless land, not a creature")
    void enchantedCreatureBecomesColorlessLand() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature can tap for colorless mana via granted ability")
    void enchantedCreatureTapsForColorless() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted Plains keeps Plains subtype but only taps for colorless")
    void enchantedPlainsKeepsSubtypeProducesColorlessOnly() {
        Permanent plains = new Permanent(new Plains());
        gd.playerBattlefields.get(player1.getId()).add(plains);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(plains.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isLand(gd, plains)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, plains).grantedSubtypes())
                .doesNotContain(CardSubtype.PLAINS);
        // Printed Plains subtype retained (not overridden)
        assertThat(plains.getCard().getSubtypes()).contains(CardSubtype.PLAINS);

        // Intrinsic white mana is gone
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted Forest retains Forest subtype and produces colorless only")
    void enchantedForestProducesColorlessOnly() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(forest);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Rejects targeting a noncreature nonland nonplaneswalker")
    void rejectsIllegalTarget() {
        Permanent auraTarget = new Permanent(new ImprisonedInTheMoon());
        // Use another Imprisoned in the Moon as an enchantment permanent on the battlefield
        gd.playerBattlefields.get(player2.getId()).add(auraTarget);

        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, auraTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing the aura restores the creature")
    void removingAuraRestoresCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isCreature(gd, bears)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.GREEN);
    }
}
