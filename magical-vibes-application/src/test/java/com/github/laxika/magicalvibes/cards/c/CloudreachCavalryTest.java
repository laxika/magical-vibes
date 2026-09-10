package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BirdMaiden;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudreachCavalry.class, BirdMaiden.class})
class CloudreachCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Is a 1/1 without flying when its controller controls no Bird")
    void noBonusWithoutBird() {
        harness.addToBattlefield(player1, new CloudreachCavalry());

        Permanent cavalry = findPermanent(player1, "Cloudreach Cavalry");

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +2/+2 and flying while its controller controls a Bird")
    void bonusWithBird() {
        harness.addToBattlefield(player1, new CloudreachCavalry());
        harness.addToBattlefield(player1, new BirdMaiden());

        Permanent cavalry = findPermanent(player1, "Cloudreach Cavalry");

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Bird does not grant the bonus")
    void opponentBirdDoesNotCount() {
        harness.addToBattlefield(player1, new CloudreachCavalry());
        harness.addToBattlefield(player2, new BirdMaiden());

        Permanent cavalry = findPermanent(player1, "Cloudreach Cavalry");

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the Bird leaves the battlefield")
    void losesBonusWhenBirdLeaves() {
        harness.addToBattlefield(player1, new CloudreachCavalry());
        harness.addToBattlefield(player1, new BirdMaiden());

        Permanent cavalry = findPermanent(player1, "Cloudreach Cavalry");
        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Bird Maiden"));

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isFalse();
    }
}
