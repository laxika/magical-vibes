package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RazorfieldRhinoTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has metalcraft static boost effect with +2/+2")
    void hasCorrectEffect() {
        RazorfieldRhino card = new RazorfieldRhino();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft =
                (MetalcraftConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) metalcraft.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Base 4/4 without metalcraft (only itself as artifact)")
    void noMetalcraftBaseStats() {
        harness.addToBattlefield(player1, new RazorfieldRhino());

        Permanent rhino = findRhino();
        // Rhino is itself an artifact, so only 1 artifact — no metalcraft
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }

    @Test
    @DisplayName("Still 4/4 with only two artifacts total (itself + one)")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent rhino = findRhino();
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Gets +2/+2 with three artifacts (itself + two) becoming 6/6")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent rhino = findRhino();
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(6);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent rhino = findRhino();
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(6);

        // Remove one Spellbook — now only 2 artifacts (Rhino + 1 Spellbook)
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst()
                .ifPresent(p -> gd.playerBattlefields.get(player1.getId()).remove(p));

        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }

    // ===== Opponent's artifacts =====

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new RazorfieldRhino());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent rhino = findRhino();
        // Only 1 artifact under player1's control (Rhino itself)
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }

    // ===== Helpers =====

    private Permanent findRhino() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Razorfield Rhino"))
                .findFirst().orElseThrow();
    }
}
