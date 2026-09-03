package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Taniwha.class, Island.class, Forest.class, IronTuskElephant.class})
class TaniwhaTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, all lands you control phase out")
    void ownLandsPhaseOut() {
        harness.addToBattlefield(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToUpkeepWithTaniwhaPhasedIn();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island, forest);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("Non-land permanents you control and opponent lands are unaffected")
    void onlyOwnLandsAffected() {
        Permanent taniwha = harness.addToBattlefieldAndReturn(player1, new Taniwha());
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new IronTuskElephant());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeepWithTaniwhaPhasedIn();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(taniwha, elephant);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentIsland);
    }

    @Test
    @DisplayName("The phased-out lands phase in during the controller's next untap step")
    void landsPhaseBackIn() {
        harness.addToBattlefield(player1, new Taniwha());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

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
        harness.passUntil(TurnStep.UNTAP);
    }

    private void advanceToUpkeepWithTaniwhaPhasedIn() {
        advanceToUpkeep(player1);
        advanceToUpkeep(player1);
    }
}
