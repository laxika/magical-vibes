package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LegionAngel.class, GrizzlyBears.class})
class LegionAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a Legion Angel from outside the game and puts it into hand")
    void offersLegionAngelFromOutsideTheGame() {
        Card chosen = new LegionAngel();
        Card nonmatching = new GrizzlyBears();
        setSideboard(chosen, nonmatching);

        castLegionAngel();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(chosen);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().sourceSideboard()).isTrue();

        choose(chosen);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    @DisplayName("May decline to take a Legion Angel")
    void mayDeclineToTakeLegionAngel() {
        Card available = new LegionAngel();
        setSideboard(available);

        castLegionAngel();
        choose(null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(available);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(available);
    }

    @Test
    @DisplayName("Does not prompt when no Legion Angel is outside the game")
    void noMatchingCardNoPrompt() {
        Card nonmatching = new GrizzlyBears();
        setSideboard(nonmatching);

        castLegionAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonmatching);
    }

    private void castLegionAngel() {
        harness.setHand(player1, List.of(new LegionAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setSideboard(Card... cards) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(cards)));
    }

    private PendingInteraction.LibrarySearch pendingSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void choose(Card card) {
        PendingInteraction.LibrarySearch search = pendingSearch();
        int index = card == null ? -1 : search.params().cards().indexOf(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
