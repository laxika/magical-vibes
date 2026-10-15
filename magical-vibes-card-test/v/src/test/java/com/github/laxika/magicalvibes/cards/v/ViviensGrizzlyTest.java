package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JaceWielderOfMysteries;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViviensGrizzly.class, Forest.class, JaceWielderOfMysteries.class})
class ViviensGrizzlyTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting a creature card reveals it and puts it into hand")
    void acceptsCreatureCard() {
        Card creature = new ViviensGrizzly();
        Card below = new Forest();
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
        Card below = new ViviensGrizzly();
        activateWithLibrary(planeswalker, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Declining a matching card puts it on the bottom of the library")
    void declinesMatchingCardToBottom() {
        Card creature = new ViviensGrizzly();
        Card below = new Forest();
        activateWithLibrary(creature, below);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, creature);
    }

    @Test
    @DisplayName("A nonmatching card is put on the bottom without a choice")
    void nonmatchingCardGoesToBottom() {
        Card nonmatching = new Forest();
        Card below = new ViviensGrizzly();
        activateWithLibrary(nonmatching, below);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonmatching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, nonmatching);
    }

    @Test
    void requiresGreenMana() {
        harness.addToBattlefield(player1, new ViviensGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void requiresFourManaInTotal() {
        harness.addToBattlefield(player1, new ViviensGrizzly());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNothing() {
        activateWithLibrary();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bottomingNonmatchingCardDoesNotRevealIt() {
        Card top = new Forest();
        activateWithLibrary(top, new ViviensGrizzly());

        assertThat(gameLogContains(top.getName())).isFalse();
    }

    @Test
    void decliningPlaneswalkerDoesNotRevealIt() {
        Card top = new JaceWielderOfMysteries();
        activateWithLibrary(top, new Forest());

        assertThat(gameLogContains(top.getName())).isFalse();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
        assertThat(gd.playerDecks.get(player1.getId())).last().isEqualTo(top);
        assertThat(gameLogContains(top.getName())).isFalse();
    }

    private void activateWithLibrary(Card... cards) {
        harness.addToBattlefield(player1, new ViviensGrizzly());
        harness.setLibrary(player1, List.of(cards));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
