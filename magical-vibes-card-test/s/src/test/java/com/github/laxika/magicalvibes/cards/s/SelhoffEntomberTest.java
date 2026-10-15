package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelhoffEntomber.class, Forest.class})
class SelhoffEntomberTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and discarding a creature draws a card")
    void discardsCreatureAndDraws() {
        Permanent entomber = addCreatureReady(player1, new SelhoffEntomber());
        Forest land = new Forest();
        SelhoffEntomber discarded = new SelhoffEntomber();
        SelhoffEntomber drawn = new SelhoffEntomber();
        harness.setHand(player1, List.of(land, discarded));
        harness.setLibrary(player1, List.of(drawn));
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(entomber.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Cannot activate without a creature card to discard")
    void requiresCreatureCardToDiscard() {
        Permanent entomber = addCreatureReady(player1, new SelhoffEntomber());
        harness.setHand(player1, List.of(new Forest()));
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(entomber.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap and discard are paid before the draw resolves")
    void paysCostsBeforeResolution() {
        Permanent entomber = addCreatureReady(player1, new SelhoffEntomber());
        SelhoffEntomber discarded = new SelhoffEntomber();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        forceMainPhase(player1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);

            assertThat(entomber.isTapped()).isTrue();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature cannot be discarded even when a creature is available")
    void rejectsNoncreatureDiscardChoice() {
        Permanent entomber = addCreatureReady(player1, new SelhoffEntomber());
        Forest land = new Forest();
        SelhoffEntomber creature = new SelhoffEntomber();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(land, creature));
        harness.setLibrary(player1, List.of(drawn));
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(entomber.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent entomber = harness.addToBattlefieldAndReturn(player1, new SelhoffEntomber());
        SelhoffEntomber creature = new SelhoffEntomber();
        harness.setHand(player1, List.of(creature));
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(entomber.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Entomber cannot activate")
    void cannotActivateWhileTapped() {
        Permanent entomber = addCreatureReady(player1, new SelhoffEntomber());
        entomber.tap();
        SelhoffEntomber creature = new SelhoffEntomber();
        harness.setHand(player1, List.of(creature));
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
