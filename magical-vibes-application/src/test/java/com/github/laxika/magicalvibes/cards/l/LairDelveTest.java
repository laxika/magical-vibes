package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LairDelveTest extends BaseCardTest {

    @Test
    @DisplayName("Creature and land cards revealed go to hand")
    void creatureAndLandGoToHand() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(bears, forest, island));

        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("Non-creature non-land cards go to the bottom of the library")
    void othersGoToBottom() {
        Card shock = new Shock();
        Card forest = new Forest();
        Card island = new Island();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock, forest, island));

        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest).doesNotContain(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, shock);
    }

    @Test
    @DisplayName("Only two cards are revealed")
    void onlyTwoCardsRevealed() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card forest3 = new Forest();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest1, forest2, forest3));

        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest1, forest2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest3);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
