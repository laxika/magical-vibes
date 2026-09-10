package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HornOfGreed;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
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

@CardUsed({Conviction.class, HornOfGreed.class, SpinedWurm.class})
class ConvictionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Conviction targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());

        harness.setHand(player1, List.of(new Conviction()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, wurm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Conviction");
    }

    @Test
    @DisplayName("Conviction gives the enchanted creature +1/+3")
    void enchantedCreatureGetsBoost() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Conviction());
        aura.setAttachedTo(wurm.getId());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(7);
    }

    @Test
    @DisplayName("Conviction's boost ends when it leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Conviction());
        aura.setAttachedTo(wurm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Conviction returns to its owner's hand when its ability resolves")
    void bounceAbilityReturnsAuraToHand() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Conviction());
        aura.setAttachedTo(wurm.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wurm);
        harness.assertInHand(player1, "Conviction");
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Conviction returns to its owner's hand when enchanting an opponent's creature")
    void bounceAbilityReturnsAuraToOwnersHandWhenEnchantedCreatureIsOpponents() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new SpinedWurm());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Conviction());
        aura.setAttachedTo(wurm.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(wurm);
        harness.assertInHand(player1, "Conviction");
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Conviction fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());

        harness.setHand(player1, List.of(new Conviction()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, wurm.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wurm);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Conviction");
        harness.assertNotOnBattlefield(player1, "Conviction");
    }

    @Test
    @DisplayName("Conviction cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HornOfGreed());
        harness.setHand(player1, List.of(new Conviction()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
