package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HazelsNocturne.class, GrizzlyBears.class, LlanowarElves.class, LeoninScimitar.class})
class HazelsNocturneTest extends BaseCardTest {

    @Test
    void promptsForUpToTwoCreatureCards() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature1, creature2, new LeoninScimitar()));
        harness.setHand(player1, List.of(new HazelsNocturne()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.maxCount()).isEqualTo(2);
        assertThat(interaction.validCardIds()).containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
    }

    @Test
    void returnsSelectedCreaturesAndDrainsEachOpponent() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new HazelsNocturne()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void drainResolvesWhenNoCreatureCardIsReturned() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.setHand(player1, List.of(new HazelsNocturne()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
