package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class DriftOfTheDeadTest extends BaseCardTest {

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
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());

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
        harness.addToBattlefield(player, new DriftOfTheDead());
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drift of the Dead"))
                .findFirst()
                .orElseThrow();
    }

    private Permanent addSnowLand(Player player) {
        Permanent snowLand = new Permanent(new Plains());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowLand);
        return snowLand;
    }
}
