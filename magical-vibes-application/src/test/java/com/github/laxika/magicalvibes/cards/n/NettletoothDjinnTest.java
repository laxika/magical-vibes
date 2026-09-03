package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NettletoothDjinn.class})
class NettletoothDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of its controller's upkeep, it deals 1 damage to that player")
    void dealsOneDamageToControllerAtUpkeep() {
        addCreatureReady(player1, new NettletoothDjinn());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("It does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        addCreatureReady(player1, new NettletoothDjinn());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The damage hits the player, not the Djinn itself")
    void survivesItsOwnTrigger() {
        Permanent djinn = addCreatureReady(player1, new NettletoothDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(djinn);
    }

    @Test
    @DisplayName("Each Djinn triggers separately when its controller has multiple Djinns")
    void eachDjinnTriggersIndependently() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new NettletoothDjinn());
        addCreatureReady(player1, new NettletoothDjinn());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
