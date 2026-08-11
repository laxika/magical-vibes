package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

class SchemingSymmetryTest extends BaseCardTest {

    @Test
    @DisplayName("Each target player searches their own library and puts a chosen card on top")
    void eachTargetPlayerSearchesTheirOwnLibrary() {
        setupLibraries();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch firstSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch.params().playerId()).isEqualTo(player1.getId());
        assertThat(firstSearch.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Island");
        int bearsIndex = firstSearch.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Grizzly Bears");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(bearsIndex));

        PendingInteraction.LibrarySearch secondSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch.params().playerId()).isEqualTo(player2.getId());
        assertThat(secondSearch.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Plains");

        int plainsIndex = secondSearch.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Plains");
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(plainsIndex));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Plains");
    }

    @Test
    @DisplayName("Requires two distinct player targets")
    void requiresTwoDistinctPlayerTargets() {
        harness.setHand(player1, List.of(new SchemingSymmetry()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast() {
        harness.setHand(player1, List.of(new SchemingSymmetry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
    }

    private void setupLibraries() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Island(), new GrizzlyBears()));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new Plains(), new GrizzlyBears()));
    }
}
