package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.h.Hundroog;
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

@CardUsed({StoicChampion.class, Hundroog.class})
class StoicChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives Stoic Champion +2/+2")
    void cyclingBoostsSelf() {
        harness.addToBattlefield(player1, new StoicChampion());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Stoic Champion");
        assertThat(PermanentGrants.powerBoostOf(gd, champion)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent cycling a card gives Stoic Champion +2/+2")
    void opponentsCyclingBoostsSelf() {
        harness.addToBattlefield(player1, new StoicChampion());
        setUpCycling(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Stoic Champion");
        assertThat(PermanentGrants.powerBoostOf(gd, champion)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple cycles stack and the boost wears off at end of turn")
    void cyclesStackUntilEndOfTurn() {
        harness.addToBattlefield(player1, new StoicChampion());
        harness.setHand(player1, List.of(new Hundroog(), new Hundroog()));
        harness.setLibrary(player1, List.of(new Hundroog(), new Hundroog()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Stoic Champion");
        assertThat(PermanentGrants.powerBoostOf(gd, champion)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, champion)).isEqualTo(4);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, champion)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, champion)).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost resolves before the cycling draw, rather than during activation")
    void boostResolvesBeforeCyclingDraw() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new StoicChampion());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(PermanentGrants.powerBoostOf(gd, champion)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, champion)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hundroog");

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, champion)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, champion)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Hundroog");
        assertThat(PermanentGrants.powerBoostOf(gd, champion)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Champion boosts itself when either player cycles")
    void cyclingBoostsChampionsOfBothPlayers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StoicChampion());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StoicChampion());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("A pending boost cannot affect a Champion that reenters the battlefield")
    void pendingBoostDoesNotAffectNewPermanent() {
        StoicChampion card = new StoicChampion();
        Permanent original = harness.addToBattlefieldAndReturn(player1, card);
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, returned)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, returned)).isZero();
        harness.assertInHand(player1, "Hundroog");
    }

    private void setUpCycling(Player player) {
        harness.setHand(player, List.of(new Hundroog()));
        harness.setLibrary(player, List.of(new Hundroog()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
