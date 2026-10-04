package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarshScrutiny.class, GrizzlyBears.class, Peek.class})
class HarshScrutinyTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature card discards it, then prompts for scry 1")
    void discardsCreatureThenScries() {
        Card creature = new GrizzlyBears();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, instant)));
        harness.setHand(player1, List.of(new HarshScrutiny()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Peek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Noncreature cards cannot be chosen")
    void onlyCreatureCardsAreValidChoices() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new HarshScrutiny()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("The spell can target only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new HarshScrutiny()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("A hand with no creatures still allows the caster to scry to the bottom")
    void scriesWhenNoCreatureCanBeChosen() {
        Card noncreature = new HarshScrutiny();
        Card top = new HarshScrutiny();
        Card next = new HarshScrutiny();
        harness.setHand(player2, List.of(noncreature));
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new HarshScrutiny()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty opposing hand still allows scrying and keeping the top card")
    void scriesWhenOpponentsHandIsEmpty() {
        Card top = new HarshScrutiny();
        Card next = new HarshScrutiny();
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new HarshScrutiny()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent discarding the chosen creature")
    void discardsWithEmptyLibrary() {
        Card chosen = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        harness.setHand(player2, List.of(chosen, remaining));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HarshScrutiny()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
