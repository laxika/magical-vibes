package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaervekTheSpiteful.class, GrizzlyBears.class})
class KaervekTheSpitefulTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures controlled by either player get -1/-1")
    void debuffsOtherCreatures() {
        harness.addToBattlefield(player1, new KaervekTheSpiteful());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not affect Kaervek itself")
    void doesNotAffectItself() {
        Permanent kaervek = harness.addToBattlefieldAndReturn(player1, new KaervekTheSpiteful());

        assertThat(gqs.getEffectivePower(gd, kaervek)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kaervek)).isEqualTo(2);
    }

    @Test
    @DisplayName("The penalty disappears when Kaervek leaves the battlefield")
    void penaltyDisappearsWhenKaervekLeaves() {
        harness.addToBattlefield(player1, new KaervekTheSpiteful());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kaervek, the Spiteful"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opposing Kaerveks affect each other and their penalties stack on other creatures")
    void opposingKaerveksStackAndKillZeroToughnessCreatures() {
        Permanent ownKaervek = harness.addToBattlefieldAndReturn(player1, new KaervekTheSpiteful());
        Permanent opponentKaervek = harness.addToBattlefieldAndReturn(player2, new KaervekTheSpiteful());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownKaervek)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownKaervek)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentKaervek)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentKaervek)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isZero();

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Kaervek, the Spiteful");
        harness.assertOnBattlefield(player2, "Kaervek, the Spiteful");
    }
}
