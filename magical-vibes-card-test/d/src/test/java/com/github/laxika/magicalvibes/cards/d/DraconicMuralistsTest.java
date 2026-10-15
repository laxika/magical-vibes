package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BronzeWalrus;
import com.github.laxika.magicalvibes.cards.e.EyesOfTheBeholder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DraconicMuralists.class, EyesOfTheBeholder.class, BronzeWalrus.class})
class DraconicMuralistsTest extends BaseCardTest {

    @Test
    @DisplayName("When Draconic Muralists dies, its controller may search for a Dragon")
    void deathCreatesMayPrompt() {
        killMuralists();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the search offers only Dragon cards and puts the chosen card into hand")
    void acceptingSearchFindsDragon() {
        harness.setLibrary(player1, List.of(new DraconicMuralists(), new BronzeWalrus()));
        killMuralists();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement().extracting(Card::getName).isEqualTo("Draconic Muralists");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Draconic Muralists");
    }

    @Test
    @DisplayName("Declining the search does not search the library")
    void decliningSearchSkipsSearch() {
        harness.setLibrary(player1, List.of(new DraconicMuralists()));
        killMuralists();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("A library without a Dragon completes the search without finding a card")
    void searchWithNoDragonCompletes() {
        harness.setLibrary(player1, List.of(new BronzeWalrus()));
        killMuralists();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Draconic Muralists");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("shuffled"));
    }

    @Test
    @DisplayName("The controller may fail to find even when a Dragon is available")
    void mayFailToFindAvailableDragon() {
        Card dragon = new DraconicMuralists();
        harness.setLibrary(player1, List.of(dragon));
        killMuralists();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dragon);
        harness.assertNotInHand(player1, "Draconic Muralists");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("shuffled"));
    }

    @Test
    @DisplayName("Accepting a search with an empty library completes without finding a card")
    void emptyLibrarySearchCompletes() {
        harness.setLibrary(player1, List.of());
        killMuralists();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInHand(player1, "Draconic Muralists");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("shuffled"));
    }

    private void killMuralists() {
        harness.addToBattlefield(player1, new DraconicMuralists());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new EyesOfTheBeholder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Draconic Muralists"));
        harness.passBothPriorities();
    }
}
