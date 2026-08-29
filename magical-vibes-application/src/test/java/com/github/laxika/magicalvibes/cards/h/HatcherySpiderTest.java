package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HatcherySpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger reveals as many cards as there are creature cards in the graveyard")
    void revealsCreatureCardsInGraveyardCount() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new ColossalDreadmaw();
        Card belowReveal = new LlanowarElves();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(eligible, tooExpensive, belowReveal));

        castHatcherySpider();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowReveal, tooExpensive);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hatchery Spider");
    }

    @Test
    @DisplayName("The controller may decline the green permanent and the revealed cards go to the bottom")
    void mayDeclinePuttingPermanentOntoBattlefield() {
        Card eligible = new LlanowarElves();
        Card belowReveal = new GrizzlyBears();

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(eligible, belowReveal));

        castHatcherySpider();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowReveal, eligible);
    }

    @Test
    @DisplayName("With no creature cards in the graveyard, the cast trigger reveals nothing")
    void noCreatureCardsRevealNothing() {
        Card topCard = new GrizzlyBears();

        harness.setGraveyard(player1, List.of(new Blaze()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(topCard);

        castHatcherySpider();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hatchery Spider");
    }

    private void castHatcherySpider() {
        harness.setHand(player1, List.of(new HatcherySpider()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
