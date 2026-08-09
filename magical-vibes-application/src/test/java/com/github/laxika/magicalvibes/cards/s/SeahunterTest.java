package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeahunterTest extends BaseCardTest {

    @Test
    @DisplayName("The ability offers only Merfolk permanent cards")
    void searchesForMerfolkPermanent() {
        addReadySeahunter();
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new CoralMerfolk(), new GrizzlyBears(), new HolyDay())));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Coral Merfolk");
    }

    @Test
    @DisplayName("The chosen Merfolk permanent enters the battlefield")
    void putsChosenMerfolkOntoBattlefield() {
        addReadySeahunter();
        harness.setLibrary(player1, new ArrayList<>(List.of(new CoralMerfolk())));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Coral Merfolk");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability does nothing when the library has no Merfolk permanent")
    void noMerfolkFound() {
        addReadySeahunter();
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new HolyDay())));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void addReadySeahunter() {
        harness.addToBattlefield(player1, new Seahunter());
        Permanent seahunter = findPermanent(player1, "Seahunter");
        seahunter.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
