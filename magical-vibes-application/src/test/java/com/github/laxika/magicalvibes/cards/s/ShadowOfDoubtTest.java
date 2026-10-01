package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Farseek;
import com.github.laxika.magicalvibes.cards.w.WateryGrave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowOfDoubt.class, Farseek.class, WateryGrave.class})
class ShadowOfDoubtTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents library searches for the turn and draws a card")
    void preventsSearchesAndDraws() {
        Card drawn = new WateryGrave();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new ShadowOfDoubt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersCantSearchLibrariesThisTurn).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);

        Card searchable = new WateryGrave();
        harness.setLibrary(player2, List.of(searchable));
        harness.setHand(player2, List.of(new Farseek()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(searchable);
    }

    @Test
    @DisplayName("Also prevents its controller from searching")
    void preventsControllerSearch() {
        Card drawn = new WateryGrave();
        Card searchable = new WateryGrave();
        harness.setLibrary(player1, List.of(drawn, searchable));
        harness.setHand(player1, List.of(new ShadowOfDoubt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(searchable);
    }

    @Test
    @DisplayName("The restriction expires during end-of-turn cleanup")
    void restrictionExpiresAtEndOfTurn() {
        gd.playersCantSearchLibrariesThisTurn = true;

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(gd.playersCantSearchLibrariesThisTurn).isFalse();
    }
}
