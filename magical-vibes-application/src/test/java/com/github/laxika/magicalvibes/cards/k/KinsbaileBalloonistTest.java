package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KinsbaileBalloonistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues target selection for a creature")
    void attackTriggerQueuesForTargetSelection() {
        addBalloonistReady(player1);
        addCreatureReady(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting the may grants flying to the targeted creature until end of turn")
    void acceptingMayGrantsFlying() {
        addBalloonistReady(player1);
        Permanent bears = addCreatureReady(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the may leaves the targeted creature without flying")
    void decliningMayLeavesNoFlying() {
        addBalloonistReady(player1);
        Permanent bears = addCreatureReady(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addBalloonistReady(player1);
        Permanent bears = addCreatureReady(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addBalloonistReady(Player player) {
        Permanent perm = new Permanent(new KinsbaileBalloonist());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
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
