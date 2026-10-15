package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraAscendant.class})
class SerraAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 without flying at default 20 life")
    void noBoostAtDefaultLife() {
        harness.addToBattlefield(player1, new SerraAscendant());

        Permanent ascendant = findAscendant();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isFalse();
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

    @Test
    @DisplayName("Gets +5/+5 and flying at exactly 30 life")
    void boostAtExactly30Life() {
        gd.playerLifeTotals.put(player1.getId(), 30);
        harness.addToBattlefield(player1, new SerraAscendant());

        Permanent ascendant = findAscendant();
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isTrue();
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

    @Test
    @DisplayName("Lifelink reaching 30 life immediately grants the boost after dealing one damage")
    void lifelinkReachesThreshold() {
        harness.setLife(player1, 29);
        Permanent ascendant = addCreatureReady(player1, new SerraAscendant());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 19);
        assertThat(gqs.getEffectivePower(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ascendant)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ascendant, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boosted Serra Ascendant deals six combat damage and gains six life")
    void boostedCombatDamageHasLifelink() {
        harness.setLife(player1, 30);
        addCreatureReady(player1, new SerraAscendant());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 36);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Each Serra Ascendant uses its own controller's life and boosts only itself")
    void eachControllerUsesTheirOwnLife() {
        harness.setLife(player1, 29);
        harness.setLife(player2, 30);
        Permanent first = addCreatureReady(player1, new SerraAscendant());
        Permanent second = addCreatureReady(player2, new SerraAscendant());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    private Permanent findAscendant() {
        return findPermanent(player1, "Serra Ascendant");
    }
}
