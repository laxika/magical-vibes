package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImpetuousDevilsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers up to one creature defending player controls and forces it to block")
    void attackTriggerForcesChosenDefendingCreatureToBlock() {
        Permanent devils = readyCreature(player1, new ImpetuousDevils());
        Permanent defendingCreature = readyCreature(player2, new GrizzlyBears());
        Permanent ownCreature = readyCreature(player1, new GrizzlyBears());
        Permanent defendingNoncreature = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(defendingNoncreature);

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(defendingCreature.getId())
                .doesNotContain(devils.getId(), ownCreature.getId(), defendingNoncreature.getId());

        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        assertThat(defendingCreature.getMustBlockIds()).containsExactly(devils.getId());
    }

    @Test
    @DisplayName("Declining the optional attack target does not impose a block requirement")
    void attackTriggerTargetCanBeDeclined() {
        Permanent devils = readyCreature(player1, new ImpetuousDevils());
        readyCreature(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent blocker = findPermanent(player2, "Grizzly Bears");
        assertThat(blocker.getMustBlockIds()).isEmpty();
        assertThat(devils.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The chosen creature must block the Devils when able")
    void chosenCreatureMustBlock() {
        Permanent devils = readyCreature(player1, new ImpetuousDevils());
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(devils.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("At the beginning of the end step, the Devils are sacrificed")
    void sacrificesAtEndStep() {
        Permanent devils = readyCreature(player1, new ImpetuousDevils());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(devils.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Impetuous Devils");
        harness.assertInGraveyard(player1, "Impetuous Devils");
    }

    private Permanent readyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
