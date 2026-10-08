package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Smother;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WirewoodHerald.class, Smother.class})
class WirewoodHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("When Wirewood Herald dies, its controller may search for an Elf card")
    void deathTriggerSearchesForElf() {
        WirewoodHerald elf = new WirewoodHerald();
        Smother nonElf = new Smother();
        harness.setLibrary(player1, List.of(elf, nonElf));
        killHerald();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(elf);
        assertThat(search.params().reveals()).isTrue();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonElf);
    }

    @Test
    @DisplayName("Declining Wirewood Herald's death trigger does not search")
    void decliningDeathTriggerDoesNotSearch() {
        WirewoodHerald elf = new WirewoodHerald();
        harness.setLibrary(player1, List.of(elf));
        killHerald();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elf);
        harness.assertInGraveyard(player1, "Wirewood Herald");
    }

    @Test
    @DisplayName("Accepting Wirewood Herald's death trigger with no Elf card leaves the library unchanged")
    void acceptingDeathTriggerWithNoElfLeavesLibraryUnchanged() {
        Smother nonElf = new Smother();
        harness.setLibrary(player1, List.of(nonElf));
        killHerald();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonElf);
        harness.assertInGraveyard(player1, "Wirewood Herald");
    }

    @Test
    @DisplayName("The controller may fail to find an Elf even when one is available")
    void mayFailToFindAvailableElf() {
        WirewoodHerald elf = new WirewoodHerald();
        harness.setLibrary(player1, List.of(elf));
        killHerald();
        harness.handleMayAbilityChosen(player1, true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elf);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Accepting the death trigger with an empty library completes the search")
    void emptyLibrarySearchCompletes() {
        harness.setLibrary(player1, List.of());
        killHerald();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An opponent's Herald searches that opponent's library and puts the Elf into their hand")
    void opponentsHeraldSearchesOpponentsLibrary() {
        WirewoodHerald ownElf = new WirewoodHerald();
        WirewoodHerald opponentsElf = new WirewoodHerald();
        harness.setLibrary(player1, List.of(ownElf));
        harness.setLibrary(player2, List.of(opponentsElf));
        harness.addToBattlefield(player2, new WirewoodHerald());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Wirewood Herald"));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(opponentsElf);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentsElf);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownElf);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(opponentsElf);
        harness.assertInGraveyard(player2, "Wirewood Herald");
    }

    private void killHerald() {
        harness.addToBattlefield(player1, new WirewoodHerald());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Wirewood Herald"));
        resolveAllTriggers();
    }
}
