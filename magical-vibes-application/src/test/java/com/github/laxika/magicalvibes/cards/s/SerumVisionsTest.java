package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerumVisionsTest extends BaseCardTest {

    @Test
    void drawsBeforeScriesTwo() {
        Card drawn = new Forest();
        Card scryTop = new Island();
        Card scryBottom = new Mountain();
        SerumVisions spell = new SerumVisions();
        harness.setLibrary(player1, List.of(drawn, scryTop, scryBottom));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(scryTop, scryBottom);
    }

    @Test
    void scriesTwoAndFinishesResolving() {
        Card drawn = new Forest();
        Card scryTop = new Island();
        Card scryBottom = new Mountain();
        SerumVisions spell = new SerumVisions();
        harness.setLibrary(player1, List.of(drawn, scryTop, scryBottom));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scryBottom, scryTop);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }
}
