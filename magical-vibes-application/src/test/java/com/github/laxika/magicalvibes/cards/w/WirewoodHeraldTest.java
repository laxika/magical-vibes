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

    private void killHerald() {
        harness.addToBattlefield(player1, new WirewoodHerald());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Wirewood Herald"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
