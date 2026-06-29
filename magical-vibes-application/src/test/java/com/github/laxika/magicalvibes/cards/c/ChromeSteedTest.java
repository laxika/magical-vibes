package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChromeSteedTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has metalcraft static boost effect with +2/+2")
    void hasCorrectEffect() {
        ChromeSteed card = new ChromeSteed();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft = (MetalcraftConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) metalcraft.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Chrome Steed puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new ChromeSteed()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chrome Steed");
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Base 2/2 when only Chrome Steed on battlefield (1 artifact)")
    void noMetalcraftWithOnlySelf() {
        harness.addToBattlefield(player1, new ChromeSteed());

        Permanent steed = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);
    }

    @Test
    @DisplayName("Base 2/2 with Chrome Steed plus one other artifact (2 total)")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new ChromeSteed());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent steed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chrome Steed"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Gets +2/+2 (becomes 4/4) with Chrome Steed plus two other artifacts (3 total)")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new ChromeSteed());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent steed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chrome Steed"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(4);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new ChromeSteed());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent steed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chrome Steed"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(4);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new ChromeSteed());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());

        Permanent steed = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);
    }
}
