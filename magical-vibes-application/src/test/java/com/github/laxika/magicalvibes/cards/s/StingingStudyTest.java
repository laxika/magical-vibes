package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StingingStudy.class, EdgarMarkov.class, Forest.class, GrizzlyBears.class})
class StingingStudyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and loses life equal to the mana value of a commander in the command zone")
    void usesCommanderInCommandZone() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        harness.setHand(player1, List.of(new StingingStudy()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        addStudyMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts a commander you own even when an opponent controls it")
    void usesOwnedCommanderOnBattlefield() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player2, commander);
        harness.setHand(player1, List.of(new StingingStudy()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        addStudyMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prompts to choose which owned commander determines X")
    void choosesAmongMultipleCommanders() {
        Card lowValueCommander = new GrizzlyBears();
        Card highValueCommander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(lowValueCommander, highValueCommander));
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(lowValueCommander, highValueCommander)));
        harness.setHand(player1, List.of(new StingingStudy()));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        addStudyMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.StingingStudyCommanderChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(highValueCommander.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    private void addStudyMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
