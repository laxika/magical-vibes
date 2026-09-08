package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrestedCraghorn.class, GrizzlyBears.class})
class CrestedCraghornTest extends BaseCardTest {

    @Test
    @DisplayName("Provoke untaps the chosen creature and forces it to block")
    void provokeUntapsAndForcesBlock() {
        Permanent craghorn = addCreatureReady(player1, new CrestedCraghorn());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(blocker.getId());

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getMustBlockIds()).containsExactly(craghorn.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining provoke leaves the chosen creature unchanged")
    void decliningProvokeDoesNothing() {
        addCreatureReady(player1, new CrestedCraghorn());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Provoke only offers a defending player's creature")
    void provokeFiltersTargets() {
        addCreatureReady(player1, new CrestedCraghorn());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

}
