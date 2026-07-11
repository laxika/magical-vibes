package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlankingTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues attack trigger for creature target selection")
    void attackingQueuesTargetSelection() {
        addReadyTroops(player1);
        addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting attack may taps target opponent creature")
    void acceptingMayTapsOpponentCreature() {
        addReadyTroops(player1);
        Permanent bears = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining attack may leaves target creature untapped")
    void decliningMayLeavesTargetUntapped() {
        addReadyTroops(player1);
        Permanent bears = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger rejects noncreature targets")
    void attackTriggerRejectsNoncreatureTargets() {
        addReadyTroops(player1);
        harness.addToBattlefield(player2, new Telepathy());
        Permanent telepathy = gd.playerBattlefields.get(player2.getId()).getFirst();
        addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, telepathy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private Permanent addReadyTroops(Player player) {
        Permanent perm = new Permanent(new FlankingTroops());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
