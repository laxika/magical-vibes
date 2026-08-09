package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost or flying")
    void opponentArtifactDoesNotCount() {
        Permanent engineer = addCreatureReady(player1, new AerialEngineer());
        harness.addToBattlefield(player2, new LeoninScimitar());

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
        harness.addToBattlefield(player1, new LeoninScimitar());
        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, engineer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engineer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.FLYING)).isFalse();
    }
}
