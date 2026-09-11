package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrashingBoars.class, RagingGoblin.class})
class CrashingBoarsTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player chooses an untapped creature and it must block Crashing Boars")
    void defendingPlayerChoosesUntappedCreature() {
        Permanent boars = addCreatureReady(player1, new CrashingBoars());
        Permanent chosenBlocker = addCreatureReady(player2, new RagingGoblin());
        Permanent otherBlocker = addCreatureReady(player2, new RagingGoblin());
        Permanent tappedCreature = addCreatureReady(player2, new RagingGoblin());
        tappedCreature.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
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
        addCreatureReady(player1, new CrashingBoars());
        Permanent tappedCreature = addCreatureReady(player2, new RagingGoblin());
        tappedCreature.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(tappedCreature.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("A sole untapped creature is selected automatically")
    void soleUntappedCreatureIsSelectedAutomatically() {
        Permanent boars = addCreatureReady(player1, new CrashingBoars());
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(blocker.getMustBlockIds()).containsExactly(boars.getId());
    }

    @Test
    @DisplayName("The chosen creature does not have to block when it is no longer able to do so")
    void chosenCreatureDoesNotHaveToBlockWhenNoLongerAble() {
        Permanent boars = addCreatureReady(player1, new CrashingBoars());
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(blocker.getMustBlockIds()).containsExactly(boars.getId());

        blocker.tap();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }
}
