package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathDeniedTest extends BaseCardTest {

    @Test
    @DisplayName("Returns exactly X target creature cards from your graveyard to your hand")
    void returnsExactlyXCreatureCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DeathDenied()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Death Denied");
    }

    @Test
    @DisplayName("Only creature cards in your graveyard are legal targets")
    void onlyCreatureCardsAreLegalTargets() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new DeathDenied()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 1, null);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Must choose exactly X targets")
    void mustChooseExactlyXTargets() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DeathDenied()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 2, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 resolves without returning any cards")
    void xZeroDoesNothing() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DeathDenied()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Death Denied");
    }
}
