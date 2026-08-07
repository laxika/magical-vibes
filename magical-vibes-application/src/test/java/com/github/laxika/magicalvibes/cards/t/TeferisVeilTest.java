package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PhaseOutAtEndOfCombat;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeferisVeilTest extends BaseCardTest {

    @Test
    @DisplayName("An attacking creature you control phases out at end of combat, not immediately")
    void attackerPhasesOutAtEndOfCombat() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();

        // The phasing is delayed to end of combat, so the attacker still dealt its combat damage.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("The phased-out attacker phases in during its controller's next untap step")
    void phasedOutAttackerPhasesBackIn() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);

        endTurn(player1); // player2's untap step — not its controller's, so it stays phased out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);

        endTurn(player2); // player1's untap step — it phases in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    private void endTurn(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature that stays back does not phase out")
    void nonAttackerDoesNotPhaseOut() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(PhaseOutAtEndOfCombat.class)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bystander);
    }

    @Test
    @DisplayName("An opponent's attacking creature is unaffected")
    void opponentAttackerDoesNotPhaseOut() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of()))
                .doesNotContain(attacker);
    }
}
