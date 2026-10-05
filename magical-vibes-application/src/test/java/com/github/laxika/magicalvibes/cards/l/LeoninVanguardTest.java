package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninVanguard.class, ChildOfNight.class, Disperse.class})
class LeoninVanguardTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Permanent vanguard(Player owner) {
        return findPermanent(owner, "Leonin Vanguard");
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
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(vanguard(player1).getPowerModifier()).isEqualTo(0);
        assertThat(vanguard(player1).getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Losing the third creature before resolution prevents both effects")
    void creatureCountIsCheckedAgainOnResolution() {
        harness.addToBattlefield(player1, new LeoninVanguard());
        Permanent support = harness.addToBattlefieldAndReturn(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new ChildOfNight());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, support.getId());
        harness.passBothPriorities();

        assertThat(vanguard(player1).getPowerModifier()).isZero();
        assertThat(vanguard(player1).getToughnessModifier()).isZero();
        harness.assertLife(player1, startingLife);
    }

    @Test
    @DisplayName("Life is still gained when Vanguard leaves but three other creatures remain")
    void sourceLeavingDoesNotPreventLifeGainWhenConditionStillHolds() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LeoninVanguard());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new ChildOfNight());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Vanguard");
        harness.assertInHand(player1, "Leonin Vanguard");
        harness.assertLife(player1, startingLife + 1);
    }

    @Test
    @DisplayName("Adding a third creature after combat begins does not create a trigger")
    void conditionMustHoldWhenCombatBegins() {
        harness.addToBattlefield(player1, new LeoninVanguard());
        harness.addToBattlefield(player1, new ChildOfNight());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        harness.enterBattlefieldAndReturn(player1, new ChildOfNight());
        harness.passBothPriorities();

        assertThat(vanguard(player1).getPowerModifier()).isZero();
        assertThat(vanguard(player1).getToughnessModifier()).isZero();
        harness.assertLife(player1, startingLife);
    }
}
