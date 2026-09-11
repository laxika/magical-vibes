package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.w.WuInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KongmingSleepingDragon.class, WuInfantry.class})
class KongmingSleepingDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void buffsOtherOwnCreatures() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new WuInfantry());
        harness.addToBattlefield(player1, new KongmingSleepingDragon());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Also buffs a creature that enters after Kongming")
    void buffsCreatureEnteringAfterSource() {
        harness.addToBattlefield(player1, new KongmingSleepingDragon());
        Permanent infantry = harness.enterBattlefieldAndReturn(player1, new WuInfantry());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent kongming = harness.addToBattlefieldAndReturn(player1, new KongmingSleepingDragon());

        assertThat(gqs.getEffectivePower(gd, kongming)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kongming)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new KongmingSleepingDragon());
        Permanent opponentInfantry = harness.addToBattlefieldAndReturn(player2, new WuInfantry());

        assertThat(gqs.getEffectivePower(gd, opponentInfantry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentInfantry)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus is removed when Kongming leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new KongmingSleepingDragon());
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new WuInfantry());

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof KongmingSleepingDragon);

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(1);
    }
}
