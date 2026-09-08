package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceWielderOfMysteries;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViviensGrizzly.class, GrizzlyBears.class, JaceWielderOfMysteries.class, LightningBolt.class})
class ViviensGrizzlyTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting a creature card reveals it and puts it into hand")
    void acceptsCreatureCard() {
        Card creature = new GrizzlyBears();
        Card below = new LightningBolt();
        activateWithLibrary(creature, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Accepting a planeswalker card reveals it and puts it into hand")
    void acceptsPlaneswalkerCard() {
        Card planeswalker = new JaceWielderOfMysteries();
        Card below = new GrizzlyBears();
        activateWithLibrary(planeswalker, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Declining a matching card puts it on the bottom of the library")
    void declinesMatchingCardToBottom() {
        Card creature = new GrizzlyBears();
        Card below = new LightningBolt();
        activateWithLibrary(creature, below);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, creature);
    }

    @Test
    @DisplayName("A nonmatching card is put on the bottom without a choice")
    void nonmatchingCardGoesToBottom() {
        Card nonmatching = new LightningBolt();
        Card below = new GrizzlyBears();
        activateWithLibrary(nonmatching, below);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonmatching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, nonmatching);
    }

    private void activateWithLibrary(Card topCard, Card belowTop) {
        harness.addToBattlefield(player1, new ViviensGrizzly());
        harness.setLibrary(player1, List.of(topCard, belowTop));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
