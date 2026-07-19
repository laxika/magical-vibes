package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JestersCapTest extends BaseCardTest {

    private void addCapReady() {
        harness.addToBattlefield(player1, new JestersCap());
        Permanent cap = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Jester's Cap"))
                .findFirst().orElseThrow();
        cap.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    @Test
    @DisplayName("Exiles three chosen cards from target player's library and shuffles")
    void exilesThreeCards() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card swamp = new Swamp();
        Card bears2 = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears, shock, swamp, bears2));

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Pick three cards (each pick re-presents the shrinking library from index 0)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Three cards left the library, one remains
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        // Exiled cards are owned by the target player and, being an unrevealed search, face down
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.exiledCards).allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
        // No further interaction pending
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        // Jester's Cap was sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Jester's Cap"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Jester's Cap"));
    }

    @Test
    @DisplayName("Exiles all cards when the library has fewer than three")
    void exilesFewerWhenLibrarySmall() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears, shock));

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Empty target library exiles nothing but still sacrifices the cap")
    void emptyLibrary() {
        gd.playerDecks.get(player2.getId()).clear();

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Jester's Cap"));
    }
}
