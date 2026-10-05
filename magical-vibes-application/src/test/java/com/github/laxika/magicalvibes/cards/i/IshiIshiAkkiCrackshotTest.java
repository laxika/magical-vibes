package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.h.HeedTheMists;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IshiIshiAkkiCrackshot.class, HeedTheMists.class, KamiOfFalseHope.class, GoblinCohort.class,
        FirstVolley.class})
class IshiIshiAkkiCrackshotTest extends BaseCardTest {

    /** Player1 controls Ishi-Ishi; it is player2's (the opponent's) turn. */
    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new IshiIshiAkkiCrackshot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's Arcane spell: Ishi-Ishi deals 2 damage to that player")
    void opponentArcaneDealsDamage() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new HeedTheMists(), "{3}{U}{U}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's Spirit creature spell also triggers the damage")
    void opponentSpiritDealsDamage() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new KamiOfFalseHope(), "{W}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's non-Spirit, non-Arcane spell does not trigger")
    void opponentUnrelatedSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new GoblinCohort(), "{R}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Controller's own Arcane spell does not trigger")
    void ownArcaneDoesNotTrigger() {
        harness.addToBattlefield(player1, new IshiIshiAkkiCrackshot());
        harness.castFromHand(player1, new HeedTheMists(), "{3}{U}{U}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Controller's own Spirit spell does not trigger")
    void ownSpiritDoesNotTrigger() {
        harness.addToBattlefield(player1, new IshiIshiAkkiCrackshot());
        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
        harness.assertOnBattlefield(player1, "Kami of False Hope");
    }

    @Test
    @DisplayName("Triggered damage still resolves after Ishi-Ishi leaves the battlefield")
    void damageResolvesAfterSourceLeavesBattlefield() {
        setUpOpponentTurn();
        var ishiIshiId = harness.getPermanentId(player1, "Ishi-Ishi, Akki Crackshot");
        harness.castFromHand(player2, new KamiOfFalseHope(), "{W}");
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, ishiIshiId);

        harness.assertInGraveyard(player1, "Ishi-Ishi, Akki Crackshot");
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, opponentLifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player2, opponentLifeBefore - 2);
        harness.assertNotOnBattlefield(player2, "Kami of False Hope");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kami of False Hope");
        harness.assertLife(player2, opponentLifeBefore - 2);
    }
}
