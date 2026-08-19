package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

class EladamrisCallTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a creature card and puts it into hand")
    void searchesForCreatureCard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Island()));
        cast();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(creature);

        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Island");
    }

    @Test
    @DisplayName("Does not offer noncreature cards")
    void doesNotOfferNoncreatures() {
        harness.setLibrary(player1, List.of(new Island()));
        cast();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNull();
    }

    private void cast() {
        harness.setHand(player1, List.of(new EladamrisCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
    }
}
