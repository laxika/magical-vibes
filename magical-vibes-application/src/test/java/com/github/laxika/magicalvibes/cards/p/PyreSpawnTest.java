package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyreSpawn.class, GrizzlyBears.class, DoomBlade.class})
class PyreSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("When Pyre Spawn dies, it deals 3 damage to a chosen player")
    void deathTriggerDealsDamageToPlayer() {
        Permanent spawn = harness.addToBattlefieldAndReturn(player1, new PyreSpawn());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, spawn.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("When Pyre Spawn dies, it deals 3 damage to a chosen creature")
    void deathTriggerDealsDamageToCreature() {
        Permanent spawn = harness.addToBattlefieldAndReturn(player1, new PyreSpawn());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, spawn.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }
}
