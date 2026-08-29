package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderBot.class, GrizzlyBears.class, Plains.class})
class SpiderBotTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability puts a chosen basic land on top of the library")
    void acceptsBasicLandSearch() {
        Plains plains = new Plains();
        GrizzlyBears bears = new GrizzlyBears();
        castSpiderBot(List.of(bears, plains));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().cards()).containsExactly(plains);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability does not search")
    void declinesBasicLandSearch() {
        Plains plains = new Plains();
        GrizzlyBears bears = new GrizzlyBears();
        castSpiderBot(List.of(plains, bears));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, bears);
    }

    private void castSpiderBot(List<Card> library) {
        harness.setHand(player1, List.of(new SpiderBot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(library);
        harness.castCreature(player1, 0);
    }

    private void resolveEtbMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
