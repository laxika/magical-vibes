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

    @Test
    @DisplayName("Battalion requires Sontaran General itself to attack")
    void battalionDoesNotTriggerWhenOnlyOtherCreaturesAttack() {
        addCreatureReady(player1, new SontaranGeneral());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(opposing.isCantBlockThisTurn()).isFalse();
        assertThatCode(() -> declareAttackers(player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Battalion cannot target two creatures controlled by the same opponent")
    void battalionRejectsTwoTargetsFromOneOpponent() {
        addCreatureReady(player1, new SontaranGeneral());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThatCode(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(second.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Battalion still resolves after all attacking creatures leave the battlefield")
    void battalionResolvesAfterAttackersLeave() {
        Permanent general = addCreatureReady(player1, new SontaranGeneral());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        gd.playerBattlefields.get(player1.getId()).removeAll(
                List.of(general, firstAttacker, secondAttacker));
        gd.playerGraveyards.get(player1.getId()).addAll(
                List.of(general.getCard(), firstAttacker.getCard(), secondAttacker.getCard()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opposing.getId()));
        resolveAllTriggers();

        assertThat(opposing.isCantBlockThisTurn()).isTrue();
        assertThatCode(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
