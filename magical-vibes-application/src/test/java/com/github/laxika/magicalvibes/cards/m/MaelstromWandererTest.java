package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaelstromWanderer.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class MaelstromWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to creatures its controller controls, including itself")
    void grantsHasteToOwnCreatures() {
        harness.addToBattlefield(player1, new MaelstromWanderer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Maelstrom Wanderer"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cascades twice when cast")
    void cascadesTwice() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new HillGiant(), new LlanowarElves()));

        harness.setHand(player1, List.of(new MaelstromWanderer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        assertCascadeOffers("Hill Giant");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        harness.passBothPriorities();
        assertCascadeOffers("Llanowar Elves");
    }

    private void assertCascadeOffers(String cardName) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly(cardName);
    }
}
