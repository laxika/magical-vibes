package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StirringHonormancer.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class StirringHonormancerTest extends BaseCardTest {

    @Test
    @DisplayName("With no other creatures, looks at one card and puts it into hand")
    void looksAtOneCardWithNoOtherCreatures() {
        Card card = new Shock();
        harness.setLibrary(player1, List.of(card));
        castAndResolve(new StirringHonormancer());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Looks at one card per controlled creature and ignores the opponent's creatures")
    void looksAtControlledCreatureCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card first = new LightningBolt();
        Card chosen = new Shock();
        Card remaining = new Shock();
        harness.setLibrary(player1, List.of(first, chosen, remaining));
        castAndResolve(new StirringHonormancer());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)
                .validCardIds()).containsExactly(first.getId(), chosen.getId());

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Must put one looked-at card into hand")
    void cannotChooseNoCards() {
        harness.addToBattlefield(player1, new StirringHonormancer());
        Card first = new StirringHonormancer();
        Card second = new StirringHonormancer();
        harness.setLibrary(player1, List.of(first, second));
        castAndResolve(new StirringHonormancer());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("An empty library finishes without a choice or a failed draw")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        castAndResolve(new StirringHonormancer());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Stirring Honormancer");
    }

    @Test
    @DisplayName("A library shorter than the creature count puts its only card into hand")
    void shortLibraryKeepsItsOnlyCard() {
        harness.addToBattlefield(player1, new StirringHonormancer());
        Card only = new StirringHonormancer();
        harness.setLibrary(player1, List.of(only));
        castAndResolve(new StirringHonormancer());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counts creatures when the trigger resolves, excluding a departed source")
    void countsCreaturesAtResolution() {
        Card top = new StirringHonormancer();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new StirringHonormancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    private void castAndResolve(StirringHonormancer card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
