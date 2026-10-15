package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulFeast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaintedSigil.class, Shock.class, SoulFeast.class})
class TaintedSigilTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the total life lost by all players this turn (both players count)")
    void gainsLifeEqualToTotalLifeLostByAllPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TaintedSigil());

        // Two Shocks: 2 damage to the opponent and 2 to the controller — damage causes loss of life.
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Total life lost this turn = 2 (controller) + 2 (opponent) = 4.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains no life when no player lost life this turn, but is still sacrificed")
    void gainsNoLifeWhenNoLifeLost() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new TaintedSigil());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Tainted Sigil");
        harness.assertInGraveyard(player1, "Tainted Sigil");
    }

    @Test
    @DisplayName("Sacrifice is paid as a cost — the artifact is in the graveyard before the ability resolves")
    void sacrificeIsPaidAsCost() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TaintedSigil());

        // Deal 2 damage to the opponent so there is life lost this turn.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.activateAbility(player1, 0, null, null);

        // Cost paid immediately: Sigil already sacrificed, ability waiting on the stack.
        harness.assertNotOnBattlefield(player1, "Tainted Sigil");
        harness.assertInGraveyard(player1, "Tainted Sigil");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Includes life lost in response to the activated ability")
    void countsLifeLostAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TaintedSigil());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain from another Sigil does not reduce the life loss counted")
    void interveningLifeGainDoesNotOffsetLifeLost() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new TaintedSigil());
        harness.addToBattlefield(player1, new TaintedSigil());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 18);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts non-damage life loss even when the same spell restores that life")
    void countsNonDamageLifeLossDespiteMatchingLifeGain() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new TaintedSigil());
        harness.setHand(player1, List.of(new SoulFeast()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.assertLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Counts life lost before the Sigil entered the battlefield")
    void countsEarlierLifeLossWithoutBeingOnBattlefield() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock(), new TaintedSigil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Tainted Sigil");
    }

    @Test
    @DisplayName("Excludes life lost during the previous turn")
    void doesNotCountPreviousTurnsLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TaintedSigil());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Tainted Sigil");
    }

    @Test
    @DisplayName("A tapped Sigil cannot activate or pay the sacrifice cost")
    void cannotActivateWhileTapped() {
        harness.addToBattlefieldAndReturn(player1, new TaintedSigil()).tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Tainted Sigil");
        harness.assertNotInGraveyard(player1, "Tainted Sigil");
        assertThat(gd.stack).isEmpty();
    }
}
