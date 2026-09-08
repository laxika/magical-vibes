package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenSunsZenith;
import com.github.laxika.magicalvibes.cards.p.PrecognitionField;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeatheredRunestoneTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents flashback casting from graveyards")
    void preventsFlashbackCasting() {
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        setupPlayer2Active();
        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blocks nonland permanents returning from graveyards")
    void blocksNonlandPermanentFromGraveyard() {
        harness.setGraveyard(player1, List.of(testCreature()));
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocks nonland permanents entering from libraries")
    void blocksNonlandPermanentFromLibrary() {
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setHand(player1, List.of(new GreenSunsZenith()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.add(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(library).anyMatch(card -> card.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Allows lands to enter from libraries")
    void allowsLandsFromLibrary() {
        harness.addToBattlefield(player1, new WeatheredRunestone());
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.add(new Forest());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Prevents casting a spell from the top of a library")
    void preventsCastingFromLibraryTop() {
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.addToBattlefield(player1, new WeatheredRunestone());
        gd.playerDecks.get(player1.getId()).addFirst(new Shock());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card testCreature() {
        return new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
