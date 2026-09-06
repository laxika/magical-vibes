package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrazenUpstart.class, GrizzlyBears.class, Shock.class})
class BrazenUpstartTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it may reveal a creature from the top five cards")
    void deathTriggerRevealsCreatureIntoHand() {
        GrizzlyBears creature = new GrizzlyBears();
        List<Card> topCards = List.of(creature, new Shock(), new Shock(), new Shock(), new Shock());
        Permanent upstart = harness.addToBattlefieldAndReturn(player1, new BrazenUpstart());
        harness.setLibrary(player1, topCards);

        destroyWithShock(upstart);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards.subList(1, topCards.size()));
    }

    @Test
    @DisplayName("When no creature is among the top five, all cards go to the bottom")
    void deathTriggerDoesNothingWhenNoCreatureIsFound() {
        List<Card> topCards = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        Permanent upstart = harness.addToBattlefieldAndReturn(player1, new BrazenUpstart());
        harness.setLibrary(player1, topCards);

        destroyWithShock(upstart);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    private void destroyWithShock(Permanent upstart) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, upstart.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
