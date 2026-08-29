package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThresherBeastTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player chooses a land to sacrifice when Thresher Beast becomes blocked")
    void defendingPlayerChoosesLandToSacrifice() {
        Permanent attacker = addReadyCreature(player1, new ThresherBeast());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareBlock(attacker, blocker);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(mountain.getId(), forest.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Thresher Beast does not trigger when it is unblocked")
    void unblockedDoesNotTrigger() {
        Permanent attacker = addReadyCreature(player1, new ThresherBeast());
        harness.addToBattlefield(player2, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Mountain");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(player == player1);
        if (player == player1) {
            permanent.setAttackTarget(player2.getId());
        }
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();
    }
}
