package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KarplusanForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DriftOfTheDead.class, KarplusanForest.class, SnowCoveredPlains.class})
class DriftOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack because it has defender")
    void cannotAttackBecauseOfDefender() {
        Permanent drift = addDrift(player1);
        addSnowLand(player1);

        assertThat(als.canAttack(gd, drift, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Is 0/0 with no snow lands")
    void isZeroZeroWithNoSnowLands() {
        Permanent drift = addDrift(player1);

        assertThat(gqs.getEffectivePower(gd, drift)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, drift)).isEqualTo(0);
    }

    @Test
    @DisplayName("P/T equal the number of snow lands controlled")
    void ptEqualsSnowLandCount() {
        Permanent drift = addDrift(player1);
        addSnowLand(player1);
        addSnowLand(player1);
        addSnowLand(player1);

        assertThat(gqs.getEffectivePower(gd, drift)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drift)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count nonsnow lands")
    void ignoresNonsnowLands() {
        Permanent drift = addDrift(player1);
        addSnowLand(player1);
        harness.addToBattlefield(player1, new KarplusanForest());
        harness.addToBattlefield(player1, new KarplusanForest());

        assertThat(gqs.getEffectivePower(gd, drift)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drift)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count opponent snow lands")
    void ignoresOpponentSnowLands() {
        Permanent drift = addDrift(player1);
        addSnowLand(player1);
        addSnowLand(player2);
        addSnowLand(player2);

        assertThat(gqs.getEffectivePower(gd, drift)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drift)).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates dynamically as snow lands enter and leave")
    void updatesDynamically() {
        Permanent drift = addDrift(player1);
        Permanent snow = addSnowLand(player1);

        assertThat(gqs.getEffectivePower(gd, drift)).isEqualTo(1);

        addSnowLand(player1);
        assertThat(gqs.getEffectivePower(gd, drift)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drift)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(snow);
        assertThat(gqs.getEffectivePower(gd, drift)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drift)).isEqualTo(1);
    }

    private Permanent addDrift(Player player) {
        return addCreatureReady(player, new DriftOfTheDead());
    }

    private Permanent addSnowLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredPlains());
    }
}
