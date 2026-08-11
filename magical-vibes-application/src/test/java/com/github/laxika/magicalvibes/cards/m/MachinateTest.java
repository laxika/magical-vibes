package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MachinateTest extends BaseCardTest {

    @Test
    void looksAtAsManyCardsAsArtifactsYouControl() {
        harness.setHand(player1, List.of(new Machinate()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        Card chosen = new GrizzlyBears();
        Card bottomed = new LlanowarElves();
        Card untouched = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(chosen, bottomed, untouched));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, bottomed);
    }

    @Test
    void controlsNoArtifactsLeavesLibraryUnchanged() {
        harness.setHand(player1, List.of(new Machinate()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(topCard);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
