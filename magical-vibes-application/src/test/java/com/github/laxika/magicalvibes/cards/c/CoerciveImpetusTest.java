package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({CoerciveImpetus.class, GrizzlyBears.class, Mountain.class})
class CoerciveImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and is goaded")
    void enchantedCreatureGetsBoostAndIsGoaded() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castCoerciveImpetus(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with the enchanted creature draws a card and loses 1 life for the Aura controller")
    void attackingDrawsAndLosesLifeForAuraController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castCoerciveImpetus(player1, bears);
        int handSizeBeforeAttack = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeAttack + 1);
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Coercive Impetus cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new CoerciveImpetus()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castCoerciveImpetus(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new CoerciveImpetus()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 2);
        harness.castEnchantment(caster, 0, creature.getId());
        harness.passBothPriorities();
    }
}
