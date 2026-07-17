package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelsongTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage this turn")
    void preventsAllCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Angelsong()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Resolve Angelsong — sets the prevent-all-combat-damage shield for the turn.
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        addUnblockedAttacker(player1); // Grizzly Bears 2/2
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance through combat damage — attacker is unblocked but all damage is prevented.
        harness.getGameService().passPriority(gd, player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Angelsong()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Angelsong");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addUnblockedAttacker(Player player) {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
