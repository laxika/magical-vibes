package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MastermindsAcquisitionTest extends BaseCardTest {

    @Test
    @DisplayName("The library mode searches for any card and puts the chosen card into hand")
    void libraryModeSearchesLibrary() {
        Card searched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), searched));
        castWithMode(0);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().sourceSideboard()).isFalse();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).contains(searched);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(searched)));

        assertThat(gd.playerHands.get(player1.getId())).contains(searched);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(searched);
    }

    @Test
    @DisplayName("The outside-the-game mode offers all sideboard cards and puts the chosen card into hand")
    void outsideGameModeSearchesSideboard() {
        Card chosen = new Island();
        Card remaining = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(chosen, remaining)));
        castWithMode(1);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().cards()).containsExactly(chosen, remaining);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(remaining);
    }

    private void castWithMode(int mode) {
        harness.setHand(player1, List.of(new MastermindsAcquisition()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorceryWithModes(player1, 0, 1, new int[]{mode}, List.of());
    }
}
