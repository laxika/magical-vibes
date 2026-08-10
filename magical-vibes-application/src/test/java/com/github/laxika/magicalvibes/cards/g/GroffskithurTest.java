package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GroffskithurTest extends BaseCardTest {

    @Test
    @DisplayName("When Groffskithur becomes blocked, it returns a named card from its graveyard to hand")
    void returnsNamedCardFromGraveyard() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Card graveyardCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(graveyardCopy));

        declareBlock(attacker, blocker);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(graveyardCopy.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCopy.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Groffskithur");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The becomes-blocked ability may be declined")
    void mayDeclineReturn() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Card graveyardCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(graveyardCopy));

        declareBlock(attacker, blocker);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Groffskithur");
        harness.assertNotInHand(player1, "Groffskithur");
    }

    @Test
    @DisplayName("The becomes-blocked ability has no target without a named card in its controller's graveyard")
    void noNamedCardMeansNoChoice() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Groffskithur()));

        declareBlock(attacker, blocker);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new Groffskithur());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
