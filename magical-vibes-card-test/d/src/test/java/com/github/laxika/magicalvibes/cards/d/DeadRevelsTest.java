package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadRevels.class, GrizzlyBears.class, LeoninScimitar.class})
class DeadRevelsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two creature cards from the graveyard to hand")
    void returnsUpToTwoCreatureCards() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature1, creature2, artifact));
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Casting for spectacle uses {1}{B} after an opponent loses life")
    void spectacleUsesAlternateCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life this turn")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May choose zero targets even with creatures in the graveyard")
    void mayChooseZeroTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Dead Revels");
    }

    @Test
    @DisplayName("May return only one creature and cannot choose an opponent's creature")
    void returnsOneCreatureFromOwnGraveyard() {
        Card chosen = new GrizzlyBears();
        Card unchosen = new GrizzlyBears();
        Card opposing = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen, unchosen));
        harness.setGraveyard(player2, List.of(opposing));
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
    }

    @Test
    @DisplayName("Still returns the remaining legal target if another target leaves the graveyard")
    void returnsRemainingLegalTarget() {
        Card removed = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        harness.assertInGraveyard(player1, "Dead Revels");
    }

    @Test
    @DisplayName("Can cast and resolve with an empty graveyard")
    void resolvesWithoutTargets() {
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dead Revels");
    }

    @Test
    @DisplayName("The caster's life loss does not enable spectacle")
    void ownLifeLossDoesNotEnableSpectacle() {
        gd.lifeLostThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spectacle returns creatures using the alternate cost")
    void spectacleReturnsCreatures() {
        Card creature = new GrizzlyBears();
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DeadRevels()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Dead Revels");
    }

}
