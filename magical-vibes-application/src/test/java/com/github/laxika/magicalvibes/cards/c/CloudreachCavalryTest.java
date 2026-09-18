package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudreachCavalry.class, AvenEnvoy.class})
class CloudreachCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Is a 1/1 without flying when its controller controls no Bird")
    void noBonusWithoutBird() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CloudreachCavalry());

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +2/+2 and flying while its controller controls a Bird")
    void bonusWithBird() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CloudreachCavalry());
        harness.addToBattlefield(player1, new AvenEnvoy());

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Bird does not grant the bonus")
    void opponentBirdDoesNotCount() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CloudreachCavalry());
        harness.addToBattlefield(player2, new AvenEnvoy());

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the Bird leaves the battlefield")
    void losesBonusWhenBirdLeaves() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CloudreachCavalry());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(bird);

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not boost another creature while its controller controls a Bird")
    void bonusAppliesOnlyToThisCreature() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CloudreachCavalry());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }
}
