package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowerGeist.class, DawntreaderElk.class, ThoughtScour.class})
class TowerGeistTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Tower Geist puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tower Geist");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tower Geist");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tower Geist");
    }

    @Test
    @DisplayName("ETB with two cards in library enters reveal choice state")
    void etbEntersRevealChoiceState() {
        harness.setLibrary(player1, List.of(new DawntreaderElk(), new ThoughtScour()));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing a card puts it in hand and the other into graveyard")
    void choosingPutsOneInHandRestInGraveyard() {
        Card card0 = new DawntreaderElk();
        Card card1 = new ThoughtScour();
        harness.setLibrary(player1, List.of(card0, card1));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card0);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing clears awaiting state")
    void choosingClearsAwaitingState() {
        Card card0 = new DawntreaderElk();
        Card card1 = new ThoughtScour();
        harness.setLibrary(player1, List.of(card0, card1));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card in library, it automatically goes to hand")
    void oneCardInLibrary() {
        Card singleCard = new DawntreaderElk();
        harness.setLibrary(player1, List.of(singleCard));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("With empty library, nothing happens")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Putting one of the two cards into hand is mandatory")
    void cannotDeclinePuttingACardIntoHand() {
        Card first = new DawntreaderElk();
        Card second = new ThoughtScour();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Only the top two cards are affected and the remaining library keeps its order")
    void leavesCardsBelowTopTwoUntouched() {
        Card first = new DawntreaderElk();
        Card second = new ThoughtScour();
        Card third = new DawntreaderElk();
        Card fourth = new ThoughtScour();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new TowerGeist()));
        addCastingMana();

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, fourth);
    }
    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAndResolveEtb() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
