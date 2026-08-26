package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Taniwha.class, Island.class, Forest.class, GrizzlyBears.class})
class TaniwhaTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, all lands you control phase out")
    void ownLandsPhaseOut() {
        addToBattlefield(player1, new Taniwha());
        Permanent island = addToBattlefield(player1, new Island());
        Permanent forest = addToBattlefield(player1, new Forest());

        advanceToUpkeepWithTaniwhaPhasedIn();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island, forest);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("Non-land permanents you control and opponent lands are unaffected")
    void onlyOwnLandsAffected() {
        Permanent taniwha = addToBattlefield(player1, new Taniwha());
        Permanent bears = addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentIsland = addToBattlefield(player2, new Island());

        advanceToUpkeepWithTaniwhaPhasedIn();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(taniwha, bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentIsland);
    }

    @Test
    @DisplayName("The phased-out lands phase in during the controller's next untap step")
    void landsPhaseBackIn() {
        addToBattlefield(player1, new Taniwha());
        Permanent island = addToBattlefield(player1, new Island());

        advanceToUpkeepWithTaniwhaPhasedIn();
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island);

        advanceTurn(); // player2's turn — still phased out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island);

        advanceTurn(); // back to player1's untap step
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToUpkeepWithTaniwhaPhasedIn() {
        advanceToUpkeep(player1);
        advanceToUpkeep(player1);
    }

    private Permanent addToBattlefield(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
