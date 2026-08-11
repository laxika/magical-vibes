package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ManipulateFateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles three cards, shuffles, then draws a card")
    void exilesThreeThenDraws() {
        Card exiledOne = new GrizzlyBears();
        Card exiledTwo = new Shock();
        Card exiledThree = new Forest();
        Card drawn = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(exiledOne, exiledTwo, exiledThree, drawn));

        harness.setHand(player1, List.of(new ManipulateFate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(exiledOne.getId(), exiledTwo.getId(), exiledThree.getId());
        assertThat(gd.exiledCards).allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(drawn.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
