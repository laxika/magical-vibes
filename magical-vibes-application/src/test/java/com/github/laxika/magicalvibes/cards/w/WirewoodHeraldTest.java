package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WirewoodHerald.class, Terminate.class, GrizzlyBears.class})
class WirewoodHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("When Wirewood Herald dies, its controller may search for an Elf card")
    void deathTriggerSearchesForElf() {
        WirewoodHerald elf = new WirewoodHerald();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(elf, bears);
        killHerald();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(elf);
        assertThat(search.params().reveals()).isTrue();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Declining Wirewood Herald's death trigger does not search")
    void decliningDeathTriggerDoesNotSearch() {
        setLibrary(new WirewoodHerald());
        killHerald();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof WirewoodHerald);
    }

    private void killHerald() {
        harness.addToBattlefield(player1, new WirewoodHerald());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Wirewood Herald"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
