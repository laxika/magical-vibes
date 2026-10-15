package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinscrollShaman.class})
class TwinscrollShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals combat damage in both combat-damage steps")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent shaman = addCreatureReady(player1, new TwinscrollShaman());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shaman);
    }

    @Test
    @DisplayName("Attacking and blocking Shamans deal lethal damage to each other over both damage steps")
    void attackingAndBlockingShamansBothDealDamageTwice() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TwinscrollShaman());
        harness.addToBattlefield(player2, new TwinscrollShaman());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Twinscroll Shaman");
        harness.assertInGraveyard(player2, "Twinscroll Shaman");
        harness.assertNotOnBattlefield(player1, "Twinscroll Shaman");
        harness.assertNotOnBattlefield(player2, "Twinscroll Shaman");
        harness.assertLife(player2, 20);
    }
}
