package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tainted Sigil"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tainted Sigil"));
    }

    @Test
    @DisplayName("Sacrifice is paid as a cost — the artifact is in the graveyard before the ability resolves")
    void sacrificeIsPaidAsCost() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TaintedSigil());

        // Deal 2 damage to the opponent so there is life lost this turn.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        // Cost paid immediately: Sigil already sacrificed, ability waiting on the stack.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tainted Sigil"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tainted Sigil"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
