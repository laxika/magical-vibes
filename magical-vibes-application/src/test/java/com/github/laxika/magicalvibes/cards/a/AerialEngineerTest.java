package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerialEngineer.class, Island.class, Manalith.class})
class AerialEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats and no flying without a controlled artifact")
    void noControlledArtifact() {
        Permanent engineer = addCreatureReady(player1, new AerialEngineer());

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +2/+0 and flying while its controller controls an artifact")
    void controlledArtifactGrantsBoostAndFlying() {
        Permanent engineer = addCreatureReady(player1, new AerialEngineer());
        harness.addToBattlefield(player1, new Manalith());

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost or flying")
    void opponentArtifactDoesNotCount() {
        Permanent engineer = addCreatureReady(player1, new AerialEngineer());
        harness.addToBattlefield(player2, new Manalith());

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the boost or flying")
    void nonArtifactPermanentDoesNotCount() {
        Permanent engineer = addCreatureReady(player1, new AerialEngineer());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the boost and flying when the controlled artifact leaves")
    void losesBoostAndFlyingWhenArtifactLeaves() {
        Permanent engineer = addCreatureReady(player1, new AerialEngineer());
        harness.addToBattlefield(player1, new Manalith());
        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Manalith"));

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains the bonus immediately when an artifact arrives")
    void gainsBonusWhenArtifactArrives() {
        Permanent engineer = addCreatureReady(player1, new AerialEngineer());
        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new Manalith());

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple artifacts grant only one bonus, which remains until the last leaves")
    void multipleArtifactsDoNotMultiplyBonus() {
        Permanent engineer = addCreatureReady(player1, new AerialEngineer());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Manalith());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Manalith());

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isFalse();
    }
}
