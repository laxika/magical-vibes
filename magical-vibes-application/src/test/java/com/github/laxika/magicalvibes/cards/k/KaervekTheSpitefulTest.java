package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KaervekTheSpitefulTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures controlled by either player get -1/-1")
    void debuffsOtherCreatures() {
        harness.addToBattlefield(player1, new KaervekTheSpiteful());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not affect Kaervek itself")
    void doesNotAffectItself() {
        harness.addToBattlefield(player1, new KaervekTheSpiteful());

        Permanent kaervek = findPermanent(player1, "Kaervek, the Spiteful");

        assertThat(gqs.getEffectivePower(gd, kaervek)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kaervek)).isEqualTo(2);
    }

    @Test
    @DisplayName("The penalty disappears when Kaervek leaves the battlefield")
    void penaltyDisappearsWhenKaervekLeaves() {
        harness.addToBattlefield(player1, new KaervekTheSpiteful());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kaervek, the Spiteful"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
