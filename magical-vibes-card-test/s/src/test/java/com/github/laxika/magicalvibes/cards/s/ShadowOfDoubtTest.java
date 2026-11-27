package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Farseek;
import com.github.laxika.magicalvibes.cards.w.WateryGrave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
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

        assertThat(playersAffectedBy(CantSearchLibrariesEffect.class))
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);

        Card searchable = new WateryGrave();
        harness.setLibrary(player2, List.of(searchable));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Farseek(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(searchable);
    }

    @Test
    @DisplayName("The log says which players the search restriction applies to")
    void logNamesThePlayersAffected() {
        harness.setLibrary(player1, List.of(new WateryGrave()));
        harness.setHand(player1, List.of(new ShadowOfDoubt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gameLogContains("Shadow of Doubt affects " + player1.getUsername() + ", "
                + player2.getUsername() + " until end of turn.")).isTrue();
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new Farseek(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(searchable);
    }

    @Test
    @DisplayName("The restriction expires during end-of-turn cleanup")
    void restrictionExpiresAtEndOfTurn() {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1.getId(),
                new CantSearchLibrariesEffect(Subjects.eachPlayer(), false), null, Duration.UNTIL_END_OF_TURN));

        gd.rulesEvents.occur(new TurnEvent.Cleanup(gd.activePlayerId));

        assertThat(gd.hasFloatingEffectOnPlayer(null, CantSearchLibrariesEffect.class)).isFalse();
    }

    @Test
    @DisplayName("Stops a search spell already on the stack")
    void preventsSearchWhenCastInResponse() {
        Card searchable = new WateryGrave();
        Card drawn = new WateryGrave();
        harness.setLibrary(player1, List.of(searchable));
        harness.setLibrary(player2, List.of(drawn));
        harness.castFromHand(player1, new Farseek(), "{1}{G}");

        harness.setHand(player2, List.of(new ShadowOfDoubt()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(searchable);
        harness.assertNotOnBattlefield(player1, "Watery Grave");
        harness.assertInGraveyard(player1, "Farseek");
        harness.assertInGraveyard(player2, "Shadow of Doubt");
    }

    @Test
    @DisplayName("A resolved restriction allows searching again after cleanup")
    void allowsSearchAfterCleanup() {
        Card drawn = new WateryGrave();
        Card searchable = new WateryGrave();
        harness.setLibrary(player1, List.of(drawn, searchable));
        harness.setHand(player1, List.of(new ShadowOfDoubt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        gd.rulesEvents.occur(new TurnEvent.Cleanup(gd.activePlayerId));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Farseek(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Watery Grave");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Farseek");
    }
}
