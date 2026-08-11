package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThassasEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Thassa's Emissary draws a card when it deals combat damage to a player")
    void drawsOnCombatDamageToPlayerAsCreature() {
        Permanent emissary = addCreatureReady(player1, new ThassasEmissary());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        emissary.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and draws when it deals combat damage")
    void bestowBoostsAndGrantsCombatDamageTrigger() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        int handSizeBeforeCombat = gd.playerHands.get(player1.getId()).size();
        bear.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBeforeCombat + 1);
    }

    @Test
    @DisplayName("A bestowed Thassa's Emissary becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent emissary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(emissary);
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
    }
}
