package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrashingBoarsTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player chooses an untapped creature and it must block Crashing Boars")
    void defendingPlayerChoosesUntappedCreature() {
        Permanent boars = readyCreature(player1, new CrashingBoars());
        Permanent chosenBlocker = readyCreature(player2, new GrizzlyBears());
        Permanent otherBlocker = readyCreature(player2, new GrizzlyBears());
        Permanent tappedCreature = readyCreature(player2, new GrizzlyBears());
        tappedCreature.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosenBlocker.getId(), otherBlocker.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DefendingPlayerChoosesCreatureToBlock.class);

        harness.handlePermanentChosen(player2, chosenBlocker.getId());

        assertThat(chosenBlocker.getMustBlockIds()).containsExactly(boars.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(chosenBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped creature is not eligible for the attack trigger's choice")
    void tappedCreatureIsNotEligible() {
        readyCreature(player1, new CrashingBoars());
        Permanent tappedCreature = readyCreature(player2, new GrizzlyBears());
        tappedCreature.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(tappedCreature.getMustBlockIds()).isEmpty();
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
