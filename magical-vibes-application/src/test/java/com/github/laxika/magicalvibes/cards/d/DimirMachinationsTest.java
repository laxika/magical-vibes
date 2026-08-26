package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DimirMachinations.class, DriftOfPhantasms.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class})
class DimirMachinationsTest extends BaseCardTest {

    @Test
    void looksAtTargetPlayersTopThreeExilesAnyNumberAndReordersTheRest() {
        Card top = new Island();
        Card second = new Forest();
        Card third = new Mountain();
        Card fourth = new GrizzlyBears();
        harness.setHand(player1, List.of(new DimirMachinations()));
        harness.setLibrary(player2, List.of(top, second, third, fourth));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().targetPlayerId())
                .isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId).containsExactly(second.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsExactly(third.getId(), top.getId(), fourth.getId());
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        DimirMachinations machinations = new DimirMachinations();
        DriftOfPhantasms matchingCard = new DriftOfPhantasms();
        GrizzlyBears differentManaValue = new GrizzlyBears();
        harness.setHand(player1, List.of(machinations));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Dimir Machinations");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }
}
