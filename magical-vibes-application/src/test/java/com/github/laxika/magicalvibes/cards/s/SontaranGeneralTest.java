package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({SontaranGeneral.class, GrizzlyBears.class})
class SontaranGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion goads and stops one creature per opponent from blocking")
    void battalionGoadsAndStopsOneCreaturePerOpponentFromBlocking() {
        addCreatureReady(player1, new SontaranGeneral());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstOpponentCreature.getId()));
        resolveAllTriggers();

        assertThat(firstOpponentCreature.isCantBlockThisTurn()).isTrue();
        assertThat(secondOpponentCreature.isCantBlockThisTurn()).isFalse();

        assertThatCode(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Battalion does not trigger with fewer than two other attackers")
    void battalionDoesNotTriggerWithTooFewOtherAttackers() {
        addCreatureReady(player1, new SontaranGeneral());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(opposing.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Battalion can choose no targets")
    void battalionCanChooseNoTargets() {
        addCreatureReady(player1, new SontaranGeneral());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();
    }
}
