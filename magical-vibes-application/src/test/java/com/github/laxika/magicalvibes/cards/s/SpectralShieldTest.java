package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralShield.class, BalduvianBears.class, GiantGrowth.class, IcyManipulator.class})
class SpectralShieldTest extends BaseCardTest {

    private Permanent bearsOf(Player owner) {
        return addCreatureReady(owner, new BalduvianBears());
    }

    private Permanent enchant(Permanent bears) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpectralShield());
        aura.setAttachedTo(bears.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +0/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = bearsOf(player1);
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost goes away when Spectral Shield leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = bearsOf(player1);
        Permanent aura = enchant(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving Spectral Shield attaches it and applies its effects")
    void resolvingAttachesAndAppliesEffects() {
        Permanent bears = bearsOf(player1);
        harness.setHand(player1, List.of(new SpectralShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Spectral Shield")
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature can't be the target of spells")
    void cannotBeTargetedBySpells() {
        Permanent bears = bearsOf(player2);
        enchant(bears);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature can still be targeted by abilities")
    void canBeTargetedByAbilities() {
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        Permanent bears = bearsOf(player1);
        enchant(bears);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(icyManipulator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unenchanted creature can still be targeted by spells")
    void targetableWithoutAura() {
        Permanent bears = bearsOf(player2);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Giant Growth"));
    }

    @Test
    @DisplayName("Spectral Shield can't target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.setHand(player1, List.of(new SpectralShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Icy Manipulator");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
