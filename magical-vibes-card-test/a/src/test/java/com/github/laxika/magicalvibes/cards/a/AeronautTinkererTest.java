package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AeronautTinkerer.class, DarksteelCitadel.class, BronzeSable.class, Island.class})
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
        harness.addToBattlefield(player1, new DarksteelCitadel());

        assertThat(gqs.hasKeyword(gd, findAeronaut(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when the controlled artifact leaves the battlefield")
    void losesFlyingWhenArtifactLeavesBattlefield() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        harness.addToBattlefield(player1, new DarksteelCitadel());

        Permanent aeronaut = findAeronaut();
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Darksteel Citadel"));

        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant flying")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        harness.addToBattlefield(player2, new DarksteelCitadel());

        assertThat(gqs.hasKeyword(gd, findAeronaut(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant flying")
    void nonArtifactPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.hasKeyword(gd, findAeronaut(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying when an artifact creature resolves, without granting it to that creature")
    void gainsFlyingWhenArtifactCreatureResolves() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        Permanent aeronaut = findAeronaut();
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isFalse();

        harness.castFromHand(player1, new BronzeSable(), "{2}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Bronze Sable"), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Keeps flying until the last controlled artifact leaves, even with an opposing artifact")
    void keepsFlyingUntilLastControlledArtifactLeaves() {
        harness.addToBattlefield(player1, new AeronautTinkerer());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent lastArtifact = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.addToBattlefield(player2, new DarksteelCitadel());
        Permanent aeronaut = findAeronaut();
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(lastArtifact);
        assertThat(gqs.hasKeyword(gd, aeronaut, Keyword.FLYING)).isFalse();
    }

    private Permanent findAeronaut() {
        return findPermanent(player1, "Aeronaut Tinkerer");
    }
}
