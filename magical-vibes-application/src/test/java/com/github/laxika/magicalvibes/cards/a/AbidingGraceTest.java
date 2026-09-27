package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbidingGrace.class, EliteVanguard.class, GrizzlyBears.class})
class AbidingGraceTest extends BaseCardTest {

    private static final String GAIN_LIFE = "You gain 1 life";
    private static final String RETURN_CREATURE =
            "Return target creature card with mana value 1 from your graveyard to the battlefield";

    @Test
    @DisplayName("Gain-life mode gains 1 life at your end step")
    void gainsLifeAtEndStep() {
        harness.addToBattlefield(player1, new AbidingGrace());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.handleListChoice(player1, GAIN_LIFE);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Reanimation mode returns a mana-value-one creature from the graveyard")
    void returnsOneManaCreature() {
        EliteVanguard creature = new EliteVanguard();
        harness.addToBattlefield(player1, new AbidingGrace());
        harness.setGraveyard(player1, List.of(creature));

        advanceToEndStep(player1);
        harness.handleListChoice(player1, RETURN_CREATURE);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Elite Vanguard")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Reanimation mode cannot target a creature with mana value greater than one")
    void rejectsCreatureWithHigherManaValue() {
        harness.addToBattlefield(player1, new AbidingGrace());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);
        harness.handleListChoice(player1, RETURN_CREATURE);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new AbidingGrace());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
