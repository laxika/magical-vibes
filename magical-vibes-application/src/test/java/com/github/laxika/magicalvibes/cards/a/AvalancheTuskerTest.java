package com.github.laxika.magicalvibes.cards.a;

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

class AvalancheTuskerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets a creature defending player controls and forces it to block")
    void attacksAndForcesDefendingCreatureToBlock() {
        Permanent tusker = readyCreature(player1, new AvalancheTusker());
        Permanent defendingCreature = readyCreature(player2, new GrizzlyBears());
        Permanent ownCreature = readyCreature(player1, new GrizzlyBears());
        Permanent defendingNoncreature = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(defendingNoncreature);

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(tusker.getId(), ownCreature.getId(), defendingNoncreature.getId());

        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        assertThat(defendingCreature.getMustBlockIds()).containsExactly(tusker.getId());
    }

    @Test
    @DisplayName("The targeted creature must be declared as a blocker")
    void targetedCreatureMustBlock() {
        Permanent tusker = readyCreature(player1, new AvalancheTusker());
        Permanent defendingCreature = readyCreature(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(defendingCreature.isBlocking()).isTrue();
    }

    private Permanent readyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
