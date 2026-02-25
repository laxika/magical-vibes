package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EzurisBrigadeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has metalcraft static boost effect with +4/+4 and trample")
    void hasCorrectEffect() {
        EzurisBrigade card = new EzurisBrigade();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft =
                (MetalcraftConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) metalcraft.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(4);
        assertThat(boost.toughnessBoost()).isEqualTo(4);
        assertThat(boost.grantedKeywords()).isEqualTo(Set.of(Keyword.TRAMPLE));
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Base 4/4 without trample when no metalcraft")
    void noMetalcraftBaseStats() {
        harness.addToBattlefield(player1, new EzurisBrigade());

        Permanent brigade = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Still 4/4 without trample with only two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new EzurisBrigade());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent brigade = findBrigade();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Gets +4/+4 and trample with three artifacts")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new EzurisBrigade());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent brigade = findBrigade();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isTrue();
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost and trample when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new EzurisBrigade());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent brigade = findBrigade();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isTrue();

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Opponent's artifacts =====

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new EzurisBrigade());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent brigade = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Helpers =====

    private Permanent findBrigade() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ezuri's Brigade"))
                .findFirst().orElseThrow();
    }
}
