package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumOfStoneFangs.class, SanctumOfTranquilLight.class, SanctumOfShatteredHeights.class})
class SanctumOfStoneFangsTest extends BaseCardTest {

    @Test
    @DisplayName("At your first main phase, each opponent loses life and you gain life for each Shrine you control")
    void drainsForEachShrineYouControl() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's first main phase")
    void doesNotTriggerOnOpponentsFirstMainPhase() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsItselfButNotOpponentsShrines() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());
        harness.addToBattlefield(player2, new SanctumOfTranquilLight());

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void countsShrinesAddedAfterTriggering() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());

        advanceToPrecombatMain(player1);
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void resolvesAfterSourceLeavesUsingRemainingShrines() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());

        advanceToPrecombatMain(player1);
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNothingWhenNoShrinesRemainAtResolution() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());

        advanceToPrecombatMain(player1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerDuringSecondMainPhase() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
