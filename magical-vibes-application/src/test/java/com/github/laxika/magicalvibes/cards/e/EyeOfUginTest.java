package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EyeOfUginTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless Eldrazi spells you cast cost {2} less")
    void reducesColorlessEldraziSpells() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Emrakul, the Promised End"));
    }

    @Test
    @DisplayName("The reduction does not apply to non-Eldrazi spells")
    void doesNotReduceNonEldraziSpells() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Searches the library for a creature card and puts it into hand")
    void searchesForCreatureCard() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        GameData gameData = harness.getGameData();
        List<Card> deck = gameData.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");

        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Mountain");
    }
}
