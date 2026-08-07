package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrickleboarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and first strike during its controller's turn")
    void boostedOnControllerTurn() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new Prickleboar());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, boar, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Is a plain 3/3 without first strike during other players' turns")
    void notBoostedOnOpponentTurn() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new Prickleboar());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, boar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bonus follows the active player, so only the attacker's boar is pumped")
    void bonusFlipsWithActivePlayer() {
        Permanent ownBoar = harness.addToBattlefieldAndReturn(player1, new Prickleboar());
        Permanent enemyBoar = harness.addToBattlefieldAndReturn(player2, new Prickleboar());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, ownBoar)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownBoar, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enemyBoar)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enemyBoar, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, ownBoar)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enemyBoar)).isEqualTo(5);
    }
}
