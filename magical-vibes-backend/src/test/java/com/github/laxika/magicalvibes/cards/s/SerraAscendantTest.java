package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SerraAscendantTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has life threshold static boost effect with +5/+5 and flying at 30 life")
    void hasCorrectEffect() {
        SerraAscendant card = new SerraAscendant();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ControllerLifeThresholdConditionalEffect.class);

        ControllerLifeThresholdConditionalEffect conditional =
                (ControllerLifeThresholdConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(conditional.lifeThreshold()).isEqualTo(30);
        assertThat(conditional.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(5);
        assertThat(boost.toughnessBoost()).isEqualTo(5);
        assertThat(boost.grantedKeywords()).isEqualTo(Set.of(Keyword.FLYING));
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Below threshold (default 20 life) =====

    @Test
    @DisplayName("Base 1/1 without flying at default 20 life")
    void noBoostAtDefaultLife() {
        harness.addToBattlefield(player1, new SerraAscendant());

        Permanent ascendant = findAscendant();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Still 1/1 without flying at 29 life")
    void noBoostAt29Life() {
        gd.playerLifeTotals.put(player1.getId(), 29);
        harness.addToBattlefield(player1, new SerraAscendant());

        Permanent ascendant = findAscendant();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isFalse();
    }

    // ===== At or above threshold =====

    @Test
    @DisplayName("Gets +5/+5 and flying at exactly 30 life")
    void boostAtExactly30Life() {
        gd.playerLifeTotals.put(player1.getId(), 30);
        harness.addToBattlefield(player1, new SerraAscendant());

        Permanent ascendant = findAscendant();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Gets +5/+5 and flying above 30 life")
    void boostAbove30Life() {
        gd.playerLifeTotals.put(player1.getId(), 50);
        harness.addToBattlefield(player1, new SerraAscendant());

        Permanent ascendant = findAscendant();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isTrue();
    }

    // ===== Loses boost when life drops =====

    @Test
    @DisplayName("Loses boost and flying when life drops below 30")
    void losesBoostWhenLifeDrops() {
        gd.playerLifeTotals.put(player1.getId(), 30);
        harness.addToBattlefield(player1, new SerraAscendant());

        Permanent ascendant = findAscendant();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isTrue();

        // Life drops below 30
        gd.playerLifeTotals.put(player1.getId(), 29);
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isFalse();
    }

    // ===== Opponent's life doesn't matter =====

    @Test
    @DisplayName("Opponent's life total doesn't affect the boost")
    void opponentLifeDoesNotCount() {
        gd.playerLifeTotals.put(player2.getId(), 50);
        harness.addToBattlefield(player1, new SerraAscendant());

        Permanent ascendant = findAscendant();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isFalse();
    }

    // ===== Helpers =====

    private Permanent findAscendant() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Serra Ascendant"))
                .findFirst().orElseThrow();
    }
}
