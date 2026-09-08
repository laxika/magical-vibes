package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GorillaChieftain;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.cards.s.StormShaman;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalControl.class, GorillaChieftain.class, StormShaman.class, StormCrow.class})
class TidalControlTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life counters a target green spell")
    void payLifeCountersGreenSpell() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.setLife(player1, 20);

        GorillaChieftain chieftain = new GorillaChieftain();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, chieftain, "{2}{G}{G}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, chieftain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Gorilla Chieftain");
        harness.assertNotOnBattlefield(player2, "Gorilla Chieftain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying {2} counters a target red spell")
    void payManaCountersRedSpell() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player1, 20);

        StormShaman shaman = new StormShaman();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, shaman, "{2}{R}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Storm Shaman");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent may activate the ability, paying the life from their own total")
    void opponentMayActivate() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        GorillaChieftain chieftain = new GorillaChieftain();
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, chieftain, "{2}{G}{G}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, chieftain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Gorilla Chieftain");
    }

    @Test
    @DisplayName("An opponent may activate the mana branch too")
    void opponentMayActivateManaAbility() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        StormShaman shaman = new StormShaman();
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, shaman, "{2}{R}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, 1, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Storm Shaman");
    }

    @Test
    @DisplayName("An opponent can activate Tidal Control when their own battlefield has an earlier permanent")
    void opponentMayActivateWithOwnPermanentBeforeSource() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.addToBattlefield(player2, new StormCrow());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        StormShaman shaman = new StormShaman();
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, shaman, "{2}{R}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, 1, null, shaman.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Storm Shaman");
    }

    @Test
    @DisplayName("Cannot counter a spell that is neither red nor green")
    void cannotTargetBlueSpell() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.setLife(player1, 20);

        StormCrow crow = new StormCrow();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, crow, "{1}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, crow.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cumulative upkeep adds an age counter before charging the next full cost")
    void cumulativeUpkeepCanBePaidAtTheIncreasingCost() {
        var tidalControl = harness.addToBattlefieldAndReturn(player1, new TidalControl());
        tidalControl.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Tidal Control");
        assertThat(tidalControl.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep sacrifices Tidal Control when the cost is not paid")
    void cumulativeUpkeepSacrifices() {
        harness.addToBattlefield(player1, new TidalControl());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Tidal Control");
        harness.assertInGraveyard(player1, "Tidal Control");
    }
}
