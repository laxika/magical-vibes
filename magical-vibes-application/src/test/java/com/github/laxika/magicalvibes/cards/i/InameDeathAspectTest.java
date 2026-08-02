package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Iname, Death Aspect")
class InameDeathAspectTest extends BaseCardTest {

    @Test
    @DisplayName("ETB search puts any number of Spirit cards into the graveyard")
    void etbSearchPutsSpiritsIntoGraveyard() {
        setupAndCast(List.of(new KamiOfOldStone(), new GrizzlyBears(), new ApothecaryGeist()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Kami of Old Stone", "Apothecary Geist");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Search can stop after fewer Spirits than are available")
    void searchCanStopEarly() {
        setupAndCast(List.of(new KamiOfOldStone(), new ApothecaryGeist()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Kami of Old Stone");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Apothecary Geist");
    }

    @Test
    @DisplayName("Declining the may ability leaves the library untouched")
    void decliningSkipsSearch() {
        setupAndCast(List.of(new KamiOfOldStone()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No Spirits in the library means nothing is put into the graveyard")
    void noSpiritsFindsNothing() {
        setupAndCast(List.of(new GrizzlyBears()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void setupAndCast(List<Card> library) {
        harness.setHand(player1, List.of(new InameDeathAspect()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.setLibrary(player1, library);
    }

    private void resolveToMayPrompt() {
        harness.passBothPriorities(); // Resolve creature → ETB trigger on the stack
        harness.passBothPriorities(); // Resolve the trigger → may prompt
    }
}
