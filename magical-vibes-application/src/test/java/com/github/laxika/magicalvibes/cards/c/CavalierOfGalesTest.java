package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CavalierOfGalesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws three cards and puts two chosen cards on top in order")
    void etbDrawsThreeAndPutsTwoOnTop() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new Forest();
        Card fourth = new Island();
        Card fifth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new CavalierOfGales()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, fourth, fifth);
    }

    @Test
    @DisplayName("Death shuffles the Cavalier into its owner's library, then scries two")
    void deathShufflesIntoLibraryThenScriesTwo() {
        Card first = new Forest();
        Card second = new Island();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, new ArrayList<>(List.of(first, second, third)));
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfGales());
        cavalier.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Cavalier of Gales");

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Cavalier of Gales");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(cavalier.getCard());
    }
}
