package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.d.DauntlessCathar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaggingThoughts.class, DauntlessCathar.class, Catalog.class})
class NaggingThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing one of the top two cards puts it into hand and the other into the graveyard")
    void choosesOneCardForHandAndPutsTheOtherInGraveyard() {
        Card chosen = new DauntlessCathar();
        Card other = new DauntlessCathar();
        harness.setLibrary(player1, List.of(chosen, other));
        castNaggingThoughts();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gameData.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card in the library, it goes into hand")
    void oneCardInLibrary() {
        Card onlyCard = new DauntlessCathar();
        harness.setLibrary(player1, List.of(onlyCard));
        castNaggingThoughts();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With an empty library, nothing moves")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        castNaggingThoughts();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Discarding Nagging Thoughts exiles it and offers madness cast")
    void discardTriggersMadness() {
        NaggingThoughts thoughts = new NaggingThoughts();
        discardWithCatalog(thoughts);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(thoughts.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Choosing no cards is illegal when two cards are available")
    void cannotPutBothCardsInGraveyard() {
        Card first = new DauntlessCathar();
        Card second = new DauntlessCathar();
        harness.setLibrary(player1, List.of(first, second));
        castNaggingThoughts();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("Choosing the second card leaves cards below the top two untouched")
    void canChooseSecondCardWithoutMovingDeeperCards() {
        Card first = new DauntlessCathar();
        Card second = new DauntlessCathar();
        Card third = new DauntlessCathar();
        harness.setLibrary(player1, List.of(first, second, third));
        castNaggingThoughts();

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("Madness can cast the sorcery on an opponent's turn for {1}{U}")
    void madnessCastsAndResolvesOnOpponentsTurn() {
        NaggingThoughts thoughts = new NaggingThoughts();
        discardWithCatalog(thoughts);
        Card chosen = new DauntlessCathar();
        Card other = new DauntlessCathar();
        harness.setLibrary(player1, List.of(chosen, other));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.findExiledCard(thoughts.getId())).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen).doesNotContain(other, thoughts);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other, thoughts);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining madness puts the discarded card in its owner's graveyard")
    void decliningMadnessDoesNotResolveSpell() {
        NaggingThoughts thoughts = new NaggingThoughts();
        discardWithCatalog(thoughts);
        Card top = new DauntlessCathar();
        harness.setLibrary(player1, List.of(top));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(thoughts.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(thoughts);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Madness cannot cast without paying {1}{U}")
    void unpaidMadnessPutsCardInGraveyard() {
        NaggingThoughts thoughts = new NaggingThoughts();
        discardWithCatalog(thoughts);
        Card top = new DauntlessCathar();
        harness.setLibrary(player1, List.of(top));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(thoughts.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(thoughts);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
    }

    private void discardWithCatalog(NaggingThoughts thoughts) {
        harness.setHand(player1, List.of(new Catalog(), thoughts));
        harness.setLibrary(player1, List.of(new DauntlessCathar(), new DauntlessCathar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
    }

    private void castNaggingThoughts() {
        harness.setHand(player1, List.of(new NaggingThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
