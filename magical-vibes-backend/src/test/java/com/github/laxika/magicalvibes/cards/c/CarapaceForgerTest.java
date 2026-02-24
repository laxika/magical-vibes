package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.MetalcraftKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarapaceForgerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has metalcraft static boost effect with +2/+2 and no keyword")
    void hasCorrectEffect() {
        CarapaceForger card = new CarapaceForger();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MetalcraftKeywordEffect.class);

        MetalcraftKeywordEffect effect = (MetalcraftKeywordEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.keyword()).isNull();
        assertThat(effect.powerBoost()).isEqualTo(2);
        assertThat(effect.toughnessBoost()).isEqualTo(2);
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Base 2/2 with zero artifacts")
    void noMetalcraftWithZeroArtifacts() {
        harness.addToBattlefield(player1, new CarapaceForger());

        Permanent forger = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Base 2/2 with two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent forger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Carapace Forger"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Gets +2/+2 (becomes 4/4) with exactly three artifacts")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent forger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Carapace Forger"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(4);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent forger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Carapace Forger"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(4);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent forger = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, forger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(2);
    }
}
