package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AeronautTinkererTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have flying without a controlled artifact")
    void noFlyingWithoutControlledArtifact() {
        harness.addToBattlefield(player1, new AeronautTinkerer());

        assertThat(gqs.hasKeyword(gd, findAeronaut(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying while its controller controls an artifact")
    void hasFlyingWithControlledArtifact() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.hasKeyword(gd, findAeronaut(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when the controlled artifact leaves the battlefield")
    void losesFlyingWhenArtifactLeavesBattlefield() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent aeronaut = findAeronaut();
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant flying")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.hasKeyword(gd, findAeronaut(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant flying")
    void nonArtifactPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.hasKeyword(gd, findAeronaut(), Keyword.FLYING)).isFalse();
    }

    private Permanent findAeronaut() {
        return findPermanent(player1, "Aeronaut Tinkerer");
    }
}
