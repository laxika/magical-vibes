package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinLookout.class, RagingGoblin.class, GrizzlyBears.class})
class GoblinLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin boosts every Goblin creature")
    void sacrificesGoblinAndBoostsAllGoblins() {
        Permanent lookout = addCreatureReady(player1, new GoblinLookout());
        Permanent sacrificedGoblin = addCreatureReady(player1, new RagingGoblin());
        Permanent ownGoblin = addCreatureReady(player1, new RagingGoblin());
        Permanent opponentGoblin = addCreatureReady(player2, new RagingGoblin());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificedGoblin);
        assertThat(gqs.getEffectivePower(gd, lookout)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lookout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("The sacrifice cost only accepts Goblins")
    void sacrificeCostOnlyAcceptsGoblins() {
        addCreatureReady(player1, new GoblinLookout());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new RagingGoblin());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
