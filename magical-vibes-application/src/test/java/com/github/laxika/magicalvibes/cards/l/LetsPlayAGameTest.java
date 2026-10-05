package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LetsPlayAGame.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Millstone.class, Peek.class})
class LetsPlayAGameTest extends BaseCardTest {

    @Test
    void weakensOnlyOpponentsCreaturesUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0});

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opposingCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void eachOpponentDiscardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new GrizzlyBears(), new Peek())));

        cast(new int[]{1});

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void eachOpponentLosesThreeAndControllerGainsThree() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{2});

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void deliriumAllowsChoosingMultipleModes() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Millstone(), new GloriousAnthem()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{0, 2});

        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void cannotChooseMultipleModesWithoutDelirium() {
        harness.setHand(player1, List.of(new LetsPlayAGame()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0, 2}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allThreeModesResolveInPrintedOrderAcrossDiscardChoices() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Millstone(), new GloriousAnthem()));
        harness.setHand(player2, List.of(new Peek(), new Peek(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{2, 1, 0});

        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void losingDeliriumAfterCastingDoesNotRemoveChosenModes() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Millstone(), new GloriousAnthem()));
        harness.setHand(player1, List.of(new LetsPlayAGame()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 2}, List.of(), null);

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void deliriumStillAllowsChoosingOnlyOneMode() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Millstone(), new GloriousAnthem()));
        harness.setHand(player2, List.of(new Peek(), new Peek()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{2});

        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void fourCardsOfOnlyThreeTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new GrizzlyBears(), new Millstone()));
        harness.setHand(player1, List.of(new LetsPlayAGame()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0, 2}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player2, List.of(
                new Forest(), new GrizzlyBears(), new Millstone(), new GloriousAnthem()));
        harness.setHand(player1, List.of(new LetsPlayAGame()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0, 2}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentWithOneCardDiscardsItAndLaterModeStillResolves() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Millstone(), new GloriousAnthem()));
        harness.setHand(player2, List.of(new Peek()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{1, 2});
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Peek");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void emptyOpposingHandDoesNotPreventLaterModeResolving() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Millstone(), new GloriousAnthem()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{1, 2});

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void creaturesEnteringAfterResolutionAreNotWeakened() {
        cast(new int[]{0});

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private void cast(int[] modes) {
        harness.setHand(player1, List.of(new LetsPlayAGame()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 3, modes, List.of(), null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
