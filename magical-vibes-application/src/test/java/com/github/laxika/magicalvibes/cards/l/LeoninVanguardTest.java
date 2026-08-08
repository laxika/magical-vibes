package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeoninVanguardTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent vanguard(Player owner) {
        UUID id = harness.getPermanentId(owner, "Leonin Vanguard");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("With three creatures it gets +1/+1 and its controller gains 1 life")
    void threeCreaturesBoostsAndGainsLife() {
        harness.addToBattlefield(player1, new LeoninVanguard());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new ChildOfNight());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(vanguard(player1).getPowerModifier()).isEqualTo(1);
        assertThat(vanguard(player1).getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("With only two creatures nothing happens")
    void twoCreaturesNoEffect() {
        harness.addToBattlefield(player1, new LeoninVanguard());
        harness.addToBattlefield(player1, new ChildOfNight());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(vanguard(player1).getPowerModifier()).isEqualTo(0);
        assertThat(vanguard(player1).getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Opponent's creatures do not count toward the three")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new LeoninVanguard());
        harness.addToBattlefield(player2, new ChildOfNight());
        harness.addToBattlefield(player2, new ChildOfNight());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(vanguard(player1).getPowerModifier()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new LeoninVanguard());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new ChildOfNight());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(vanguard(player1).getPowerModifier()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("The boost wears off at end of turn but the life stays")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new LeoninVanguard());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new ChildOfNight());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(vanguard(player1).getPowerModifier()).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vanguard(player1).getPowerModifier()).isEqualTo(0);
        assertThat(vanguard(player1).getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }
}
