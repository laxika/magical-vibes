package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dispossess.class, Ornithopter.class, GrizzlyBears.class, CosisTrickster.class})
class DispossessTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting the opponent")
    void castingTargetsOpponent() {
        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Only artifact card names are offered")
    void offersOnlyArtifactNames() {
        harness.setHand(player2, new ArrayList<>(List.of(new Ornithopter(), new GrizzlyBears())));

        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Ornithopter");
        assertThat(choice.options()).doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("After name choice with matches, prompts for card selection")
    void afterNameChoicePromptsForSelection() {
        harness.setHand(player2, new ArrayList<>(List.of(new Ornithopter())));

        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Ornithopter");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
    }

    @Test
    @DisplayName("Exiles matching artifacts from the opponent's hand, graveyard, and library")
    void exilesMatchingArtifactsFromAllZones() {
        Card thopter1 = new Ornithopter();
        Card thopter2 = new Ornithopter();
        Card thopter3 = new Ornithopter();

        harness.setHand(player2, new ArrayList<>(List.of(thopter1)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(thopter2)));
        harness.setLibrary(player2, List.of(thopter3));

        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Ornithopter");
        harness.handleMultipleCardsChosen(player1, List.of(thopter1.getId(), thopter2.getId(), thopter3.getId()));

        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Ornithopter"))
                .count();
        assertThat(exiledCount).isEqualTo(3);

        harness.assertNotInHand(player2, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Choosing a name with no matches just shuffles the library")
    void noMatchesShufflesLibrary() {
        harness.setHand(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Ornithopter");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("exiles 0 cards"));
    }

    @Test
    @DisplayName("Dispossess goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Ornithopter");

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dispossess");
    }

    @Test
    @DisplayName("May exile zero cards even when copies exist in every searched zone")
    void mayChooseZeroMatchingCards() {
        Card handCopy = new Ornithopter();
        Card graveyardCopy = new Ornithopter();
        Card libraryCopy = new Ornithopter();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Ornithopter");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Dispossess");
    }

    @Test
    @DisplayName("Exiles only selected copies and leaves other names and the caster's copies alone")
    void exilesOnlySelectedCopies() {
        Card selected = new Ornithopter();
        Card unselected = new Ornithopter();
        Card libraryCopy = new Ornithopter();
        Card otherName = new GrizzlyBears();
        Card ownCopy = new Ornithopter();
        harness.setHand(player2, List.of(selected, otherName));
        harness.setGraveyard(player2, List.of(unselected));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setHand(player1, List.of(new Dispossess(), ownCopy));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Ornithopter");
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(selected);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(otherName);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unselected);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCopy);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent's shuffle triggers abilities even when no matching copies are selected")
    void shuffleTriggersAfterDecliningToExileMatches() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player1, new CosisTrickster());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Ornithopter");
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
